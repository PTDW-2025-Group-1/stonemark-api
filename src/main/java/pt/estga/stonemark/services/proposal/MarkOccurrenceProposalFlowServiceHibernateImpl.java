package pt.estga.stonemark.services.proposal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.entities.content.Monument;
import pt.estga.stonemark.entities.proposals.MarkOccurrenceProposal;
import pt.estga.stonemark.entities.proposals.MonumentData;
import pt.estga.stonemark.enums.ProposalStatus;
import pt.estga.stonemark.enums.TargetType;
import pt.estga.stonemark.dtos.proposal.ProposalStateDto;
import pt.estga.stonemark.repositories.MediaRepository;
import pt.estga.stonemark.repositories.proposals.MarkOccurrenceProposalRepository;
import pt.estga.stonemark.services.content.MonumentService;
import pt.estga.stonemark.services.file.MediaService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarkOccurrenceProposalFlowServiceHibernateImpl implements MarkOccurrenceProposalFlowService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final MediaService mediaService;
    private final MediaRepository mediaRepository;
    private final GpsExtractorService gpsExtractorService;
    private final MonumentService monumentService;
    private final MockMarkPatternSearchService markPatternSearchService;
    private static final double COORDINATE_SEARCH_RANGE = 0.01;

    @Override
    public ProposalStateDto initiateProposal(MultipartFile photo) throws IOException {
        MediaFile mediaFile = mediaService.save(photo, TargetType.PROPOSAL, 0L); // Temp ID
        MarkOccurrenceProposal proposal = new MarkOccurrenceProposal();
        proposal.setOriginalMediaFile(mediaFile);
        proposal.setStatus(ProposalStatus.IN_PROGRESS);
        MarkOccurrenceProposal savedProposal = proposalRepository.save(proposal);

        mediaFile.setTargetId(savedProposal.getId());
        mediaRepository.save(mediaFile);

        Optional<MonumentData> gpsData = gpsExtractorService.extractGpsData(mediaFile);
        if (gpsData.isPresent()) {
            List<Monument> monuments = monumentService.findByCoordinatesInRange(gpsData.get().getLatitude(), gpsData.get().getLongitude(), COORDINATE_SEARCH_RANGE);
            if (!monuments.isEmpty()) {
                savedProposal.setExistingMonument(monuments.getFirst());
                savedProposal.setStatus(ProposalStatus.AWAITING_MARK_INFO);
                proposalRepository.save(savedProposal);
                return new ProposalStateDto(savedProposal, ProposalStatus.AWAITING_MARK_INFO, "Monument found. Please provide mark details.");
            }
        }

        savedProposal.setStatus(ProposalStatus.AWAITING_MONUMENT_INFO);
        proposalRepository.save(savedProposal);
        return new ProposalStateDto(savedProposal, ProposalStatus.AWAITING_MONUMENT_INFO, "Please provide monument details.");
    }

    @Override
    public ProposalStateDto updateMonument(Long proposalId, String monumentName, double latitude, double longitude) {
        MarkOccurrenceProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        List<Monument> monuments;
        if (monumentName != null && !monumentName.isBlank()) {
            monuments = monumentService.findByNameContaining(monumentName);
        } else {
            monuments = monumentService.findByCoordinatesInRange(latitude, longitude, COORDINATE_SEARCH_RANGE);
        }

        if (!monuments.isEmpty()) {
            proposal.setExistingMonument(monuments.get(0));
        } else {
            proposal.setProposedMonumentData(MonumentData.builder().name(monumentName).latitude(latitude).longitude(longitude).build());
        }

        proposal.setStatus(ProposalStatus.AWAITING_MARK_INFO);
        proposalRepository.save(proposal);
        return new ProposalStateDto(proposal, ProposalStatus.AWAITING_MARK_INFO, "Monument updated. Please provide mark details.");
    }

    @Override
    public ProposalStateDto finalizeProposal(Long proposalId) {
        MarkOccurrenceProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        // In a real scenario, you might have another step to add mark details.
        // For now, we'll just finalize the proposal.
        proposal.setStatus(ProposalStatus.SUBMITTED);
        proposalRepository.save(proposal);
        return new ProposalStateDto(proposal, ProposalStatus.SUBMITTED, "Proposal submitted successfully.");
    }
}
