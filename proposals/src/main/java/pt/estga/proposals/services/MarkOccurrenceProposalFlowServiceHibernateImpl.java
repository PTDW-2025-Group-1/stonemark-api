package pt.estga.proposals.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Monument;
import pt.estga.content.repositories.MarkRepository;
import pt.estga.file.entities.MediaFile;
import pt.estga.file.enums.TargetType;
import pt.estga.file.services.MediaService;
import pt.estga.proposals.dtos.ProposeNewMarkRequestDto;
import pt.estga.proposals.dtos.ProposeNewMonumentRequestDto;
import pt.estga.proposals.dtos.SelectExistingMarkRequestDto;
import pt.estga.proposals.dtos.SelectExistingMonumentRequestDto;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.entities.ProposedMark;
import pt.estga.proposals.entities.ProposedMonument;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposals.repositories.ProposedMarkRepository;
import pt.estga.proposals.repositories.ProposedMonumentRepository;
import pt.estga.shared.models.Location;
import pt.estga.stonemark.repositories.content.MonumentRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarkOccurrenceProposalFlowServiceHibernateImpl implements MarkOccurrenceProposalFlowService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final MediaService mediaService;
    private final GpsExtractorService gpsExtractorService;
    private final MonumentRepository monumentRepository;
    private final MarkRepository markRepository;
    private final ProposedMarkRepository proposedMarkRepository;
    private final ProposedMonumentRepository proposedMonumentRepository;
    private static final double COORDINATE_SEARCH_RANGE = 0.01;

    private MarkOccurrenceProposal getProposalById(Long proposalId, String operation) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> {
                    log.error("Proposal with ID {} not found during {}.", proposalId, operation);
                    return new RuntimeException("Proposal not found");
                });
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal initiateProposal(byte[] photoData, String filename) throws IOException {
        log.info("Initiating proposal for file: {}", filename);
        MediaFile mediaFile = mediaService.save(photoData, filename, TargetType.PROPOSAL);
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setOriginalMediaFile(mediaFile);
        proposal.setStatus(ProposalStatus.IN_PROGRESS);
        MarkOccurrenceProposal savedProposal = proposalRepository.save(proposal);
        log.debug("Proposal initiated with ID: {}", savedProposal.getId());

        Optional<Location> gpsData = gpsExtractorService.extractGpsData(mediaFile);
        if (gpsData.isPresent()) {
            log.info("GPS data found for proposal {}: Latitude={}, Longitude={}", savedProposal.getId(), gpsData.get().getLatitude(), gpsData.get().getLongitude());
            List<Monument> monuments = monumentRepository.findByCoordinatesInRange(
                    gpsData.get().getLatitude(),
                    gpsData.get().getLongitude(),
                    COORDINATE_SEARCH_RANGE
            );
            if (!monuments.isEmpty()) {
                log.info("Existing monument found for proposal {} with ID: {}", savedProposal.getId(), monuments.getFirst().getId());
                savedProposal.setExistingMonument(monuments.getFirst());
                savedProposal.setStatus(ProposalStatus.AWAITING_MARK_INFO);
                return proposalRepository.save(savedProposal);
            } else {
                log.info("No existing monument found near GPS coordinates for proposal {}", savedProposal.getId());
            }
        } else {
            log.info("No GPS data found for proposal {}", savedProposal.getId());
        }

        savedProposal.setStatus(ProposalStatus.AWAITING_MONUMENT_INFO);
        return proposalRepository.save(savedProposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal handleExistingMonumentSelection(Long proposalId, SelectExistingMonumentRequestDto requestDto) {
        log.info("User selected existing monument with ID: {} for proposal ID: {}", requestDto.existingMonumentId(), proposalId);
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "existing monument selection");

        // Clear previous selections for monument
        proposal.setExistingMonument(null);
        proposal.setProposedMonument(null);

        monumentRepository.findById(requestDto.existingMonumentId())
                .ifPresentOrElse(
                        monument -> {
                            proposal.setExistingMonument(monument);
                            log.debug("Existing monument assigned to proposal ID: {}", proposal.getId());
                        },
                        () -> {
                            log.error("Existing monument with ID {} not found for proposal ID {}", requestDto.existingMonumentId(), proposal.getId());
                            throw new RuntimeException("Selected monument not found."); // Throw an exception
                        }
                );

        proposal.setStatus(ProposalStatus.AWAITING_MARK_INFO);
        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal handleNewMonumentProposal(Long proposalId, ProposeNewMonumentRequestDto requestDto) {
        log.info("User proposed a new monument for proposal ID: {}. Name: {}, Latitude: {}, Longitude: {}", proposalId, requestDto.name(), requestDto.latitude(), requestDto.longitude());
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "new monument proposal");

        // Clear previous selections for monument
        proposal.setExistingMonument(null);
        if (proposal.getProposedMonument() != null) {
            proposedMonumentRepository.delete(proposal.getProposedMonument());
        }
        proposal.setProposedMonument(null);

        ProposedMonument currentProposedMonument = ProposedMonument.builder().build();

        currentProposedMonument.setName(requestDto.name());
        currentProposedMonument.setLatitude(requestDto.latitude());
        currentProposedMonument.setLongitude(requestDto.longitude());

        // Explicitly save the ProposedMonument first
        ProposedMonument savedProposedMonument = proposedMonumentRepository.save(currentProposedMonument);

        proposal.setProposedMonument(savedProposedMonument);
        log.debug("Proposed monument assigned to proposal ID: {}", proposal.getId());

        proposal.setStatus(ProposalStatus.AWAITING_MARK_INFO);
        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal handleExistingMarkSelection(Long proposalId, SelectExistingMarkRequestDto requestDto) {
        log.info("User selected existing mark with ID: {} for proposal ID: {}", requestDto.existingMarkId(), proposalId);
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "existing mark selection");

        // Clear previous selections for mark
        proposal.setExistingMark(null);
        if (proposal.getProposedMark() != null) {
            proposedMarkRepository.delete(proposal.getProposedMark());
        }
        proposal.setProposedMark(null);

        markRepository.findById(requestDto.existingMarkId()).ifPresentOrElse(
                proposal::setExistingMark,
                () -> {
                    log.error("Existing mark with ID {} not found for proposal ID {}", requestDto.existingMarkId(), proposal.getId());
                    throw new RuntimeException("Selected mark not found."); // Throw an exception
                }
        );

        proposal.setStatus(ProposalStatus.READY_TO_SUBMIT);
        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal handleNewMarkProposal(Long proposalId, ProposeNewMarkRequestDto requestDto) {
        log.info("User proposed a new mark for proposal ID: {}. Name: {}", proposalId, requestDto.name());
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "new mark proposal");

        // Clear previous selections for mark
        proposal.setExistingMark(null);
        if (proposal.getProposedMark() != null) {
            proposedMarkRepository.delete(proposal.getProposedMark());
        }
        proposal.setProposedMark(null);

        ProposedMark proposedMark = new ProposedMark();

        proposedMark.setName(requestDto.name());
        proposedMark.setDescription(Optional.ofNullable(requestDto.description()).orElse(""));
        proposedMark.setMediaFile(proposal.getOriginalMediaFile());

        // Explicitly save the ProposedMark first
        ProposedMark savedProposedMark = proposedMarkRepository.save(proposedMark);

        proposal.setProposedMark(savedProposedMark);
        log.debug("Proposed mark assigned to proposal ID: {}", proposal.getId());

        proposal.setStatus(ProposalStatus.READY_TO_SUBMIT);
        return proposalRepository.save(proposal);
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal submitProposal(Long proposalId) {
        log.info("Confirming proposal with ID: {}", proposalId);
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "confirmation");

        proposal.setStatus(ProposalStatus.SUBMITTED);
        return proposalRepository.save(proposal);
    }

    @Override
    public MarkOccurrenceProposal approveProposal(Long proposalId) {
        // Todo: implement approveProposal
        return null;
    }

    @Override
    @Transactional
    public MarkOccurrenceProposal rejectProposal(Long proposalId) {
        log.info("Rejecting proposal with ID: {}", proposalId);
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "rejection");

        proposal.setStatus(ProposalStatus.REJECTED);
        return proposalRepository.save(proposal);
    }
}
