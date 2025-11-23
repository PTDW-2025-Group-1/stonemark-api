package pt.estga.stonemark.services.proposal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.dtos.proposal.ProposalStateDto;
import pt.estga.stonemark.dtos.proposal.SelectExistingMarkRequestDto;
import pt.estga.stonemark.dtos.proposal.ProposeNewMarkRequestDto;
import pt.estga.stonemark.dtos.proposal.SelectExistingMonumentRequestDto;
import pt.estga.stonemark.dtos.proposal.ProposeNewMonumentRequestDto;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.entities.content.Mark;
import pt.estga.stonemark.entities.content.Monument;
import pt.estga.stonemark.entities.proposals.MarkOccurrenceProposal;
import pt.estga.stonemark.entities.proposals.ProposedMark;
import pt.estga.stonemark.entities.proposals.ProposedMonument;
import pt.estga.stonemark.enums.ProposalStatus;
import pt.estga.stonemark.enums.TargetType;
import pt.estga.stonemark.mappers.MarkOccurrenceProposalMapper;
import pt.estga.stonemark.models.Location;
import pt.estga.stonemark.repositories.content.MarkRepository;
import pt.estga.stonemark.repositories.content.MonumentRepository;
import pt.estga.stonemark.repositories.proposals.MarkOccurrenceProposalRepository;
import pt.estga.stonemark.repositories.proposals.ProposedMarkRepository;
import pt.estga.stonemark.repositories.proposals.ProposedMonumentRepository;
import pt.estga.stonemark.services.file.MediaService;

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
    private final MarkOccurrenceProposalMapper markOccurrenceProposalMapper;
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
    public ProposalStateDto initiateProposal(byte[] photoData, String filename) throws IOException {
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
                proposalRepository.save(savedProposal);
                return new ProposalStateDto(markOccurrenceProposalMapper.toDto(savedProposal), ProposalStatus.AWAITING_MARK_INFO, "Monument found. Please provide mark details.");
            } else {
                log.info("No existing monument found near GPS coordinates for proposal {}", savedProposal.getId());
            }
        } else {
            log.info("No GPS data found for proposal {}", savedProposal.getId());
        }

        savedProposal.setStatus(ProposalStatus.AWAITING_MONUMENT_INFO);
        proposalRepository.save(savedProposal);
        log.info("Proposal {} status set to AWAITING_MONUMENT_INFO", savedProposal.getId());
        return new ProposalStateDto(markOccurrenceProposalMapper.toDto(savedProposal), ProposalStatus.AWAITING_MONUMENT_INFO, "Please provide monument details.");
    }

    @Override
    @Transactional
    public ProposalStateDto handleExistingMonumentSelection(Long proposalId, SelectExistingMonumentRequestDto requestDto) {
        log.info("User selected existing monument with ID: {} for proposal ID: {}", requestDto.getExistingMonumentId(), proposalId);
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "existing monument selection");

        // Clear previous selections for monument
        proposal.setExistingMonument(null);
        proposal.setProposedMonument(null);

        monumentRepository.findById(requestDto.getExistingMonumentId())
                .ifPresentOrElse(
                        monument -> {
                            proposal.setExistingMonument(monument);
                            log.debug("Existing monument assigned to proposal ID: {}", proposal.getId());
                        },
                        () -> {
                            log.error("Existing monument with ID {} not found for proposal ID {}", requestDto.getExistingMonumentId(), proposal.getId());
                            throw new RuntimeException("Selected monument not found."); // Throw an exception
                        }
                );

        proposal.setStatus(ProposalStatus.AWAITING_MARK_INFO);
        proposalRepository.save(proposal);
        log.info("Proposal {} status set to AWAITING_MARK_INFO after existing monument selection.", proposalId);
        return new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), ProposalStatus.AWAITING_MARK_INFO, "Existing monument selected. Please provide mark details.");
    }

    @Override
    @Transactional
    public ProposalStateDto handleNewMonumentProposal(Long proposalId, ProposeNewMonumentRequestDto requestDto) {
        log.info("User proposed a new monument for proposal ID: {}. Name: {}, Latitude: {}, Longitude: {}", proposalId, requestDto.getName(), requestDto.getLatitude(), requestDto.getLongitude());
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "new monument proposal");

        // Clear previous selections for monument
        proposal.setExistingMonument(null);
        if (proposal.getProposedMonument() != null) {
            proposedMonumentRepository.delete(proposal.getProposedMonument());
        }
        proposal.setProposedMonument(null);

        ProposedMonument currentProposedMonument = ProposedMonument.builder().build();

        currentProposedMonument.setName(requestDto.getName());
        currentProposedMonument.setLatitude(requestDto.getLatitude());
        currentProposedMonument.setLongitude(requestDto.getLongitude());

        // Explicitly save the ProposedMonument first
        ProposedMonument savedProposedMonument = proposedMonumentRepository.save(currentProposedMonument);

        proposal.setProposedMonument(savedProposedMonument);
        log.debug("Proposed monument assigned to proposal ID: {}", proposal.getId());

        proposal.setStatus(ProposalStatus.AWAITING_MARK_INFO);
        proposalRepository.save(proposal);
        log.info("Proposal {} status set to AWAITING_MARK_INFO after new monument proposal.", proposalId);
        return new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), ProposalStatus.AWAITING_MARK_INFO, "New monument proposed. Please provide mark details.");
    }

    @Override
    @Transactional
    public ProposalStateDto handleExistingMarkSelection(Long proposalId, SelectExistingMarkRequestDto requestDto) {
        log.info("User selected existing mark with ID: {} for proposal ID: {}", requestDto.getExistingMarkId(), proposalId);
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "existing mark selection");

        // Clear previous selections for mark
        proposal.setExistingMark(null);
        if (proposal.getProposedMark() != null) {
            proposedMarkRepository.delete(proposal.getProposedMark());
        }
        proposal.setProposedMark(null);

        markRepository.findById(requestDto.getExistingMarkId()).ifPresentOrElse(
                proposal::setExistingMark,
                () -> {
                    log.error("Existing mark with ID {} not found for proposal ID {}", requestDto.getExistingMarkId(), proposal.getId());
                    throw new RuntimeException("Selected mark not found."); // Throw an exception
                }
        );

        proposal.setStatus(ProposalStatus.READY_TO_SUBMIT);
        proposalRepository.save(proposal);
        log.info("Proposal {} status set to READY_TO_SUBMIT after existing mark selection.", proposalId);
        return new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), ProposalStatus.READY_TO_SUBMIT, "Existing mark selected. Please confirm the proposal.");
    }

    @Override
    @Transactional
    public ProposalStateDto handleNewMarkProposal(Long proposalId, ProposeNewMarkRequestDto requestDto) {
        log.info("User proposed a new mark for proposal ID: {}. Name: {}", proposalId, requestDto.getName());
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "new mark proposal");

        // Clear previous selections for mark
        proposal.setExistingMark(null);
        if (proposal.getProposedMark() != null) {
            proposedMarkRepository.delete(proposal.getProposedMark());
        }
        proposal.setProposedMark(null);

        ProposedMark proposedMark = new ProposedMark();

        proposedMark.setName(requestDto.getName());
        proposedMark.setDescription(Optional.ofNullable(requestDto.getDescription()).orElse(""));
        proposedMark.setMediaFile(proposal.getOriginalMediaFile());

        // Explicitly save the ProposedMark first
        ProposedMark savedProposedMark = proposedMarkRepository.save(proposedMark);

        proposal.setProposedMark(savedProposedMark);
        log.debug("Proposed mark assigned to proposal ID: {}", proposal.getId());

        proposal.setStatus(ProposalStatus.READY_TO_SUBMIT);
        proposalRepository.save(proposal);
        log.info("Proposal {} status set to READY_TO_SUBMIT after new mark proposal.", proposalId);
        return new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), ProposalStatus.READY_TO_SUBMIT, "New mark proposed. Please confirm the proposal.");
    }

    @Override
    @Transactional
    public ProposalStateDto submitProposal(Long proposalId) {
        log.info("Confirming proposal with ID: {}", proposalId);
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "confirmation");

        proposal.setStatus(ProposalStatus.SUBMITTED);
        proposalRepository.save(proposal);
        log.info("Proposal {} status set to SUBMITTED.", proposalId);
        return new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), ProposalStatus.SUBMITTED, "Proposal submitted successfully.");
    }

    @Override
    public ProposalStateDto approveProposal(Long proposalId) {
        // Todo: implement approveProposal
        return null;
    }

    @Override
    @Transactional
    public ProposalStateDto rejectProposal(Long proposalId) {
        log.info("Rejecting proposal with ID: {}", proposalId);
        MarkOccurrenceProposal proposal = getProposalById(proposalId, "rejection");

        proposal.setStatus(ProposalStatus.REJECTED);
        proposalRepository.save(proposal);
        log.info("Proposal {} status set to REJECTED.", proposalId);
        return new ProposalStateDto(markOccurrenceProposalMapper.toDto(proposal), ProposalStatus.REJECTED, "Proposal has been rejected.");
    }
}
