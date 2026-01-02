package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.content.dtos.GeocodingResultDto;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MonumentService;
import pt.estga.content.services.ReverseGeocodingService;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonumentCreationService {

    private final MonumentService monumentService;
    private final ReverseGeocodingService reverseGeocodingService;
    private final MarkOccurrenceProposalRepository proposalRepo;

    public void ensureMonumentExists(MarkOccurrenceProposal proposal) {
        if (proposal.getExistingMonument() != null) {
            return;
        }

        String monumentName = proposal.getMonumentName();
        String address = null;
        String city = null;

        // Fetch geocoding info if coordinates are available
        if (proposal.getLatitude() != null && proposal.getLongitude() != null) {
            try {
                GeocodingResultDto geocodingResult = reverseGeocodingService.reverseGeocode(proposal.getLatitude(), proposal.getLongitude());
                if (geocodingResult != null) {
                    if (monumentName == null) {
                        monumentName = geocodingResult.getName();
                    }
                    address = geocodingResult.getAddress();
                    city = geocodingResult.getCity();
                }
            } catch (Exception e) {
                log.error("Failed to fetch geocoding info for monument creation", e);
            }
        }

        if (monumentName == null) {
            log.warn("Cannot create monument for proposal ID {}: No name provided or found.", proposal.getId());
            return;
        }
        
        // Update proposal with found name if it was missing
        if (proposal.getMonumentName() == null) {
            proposal.setMonumentName(monumentName);
        }

        Optional<Monument> existingMonument = monumentService.findByName(monumentName);

        if (existingMonument.isPresent()) {
            log.info("Monument with name '{}' already exists. Linking proposal to existing monument.", monumentName);
            proposal.setExistingMonument(existingMonument.get());
            proposalRepo.save(proposal);
        } else {
            log.info("Creating new monument '{}' from proposal ID: {}", monumentName, proposal.getId());

            Monument newMonument = Monument.builder()
                    .name(monumentName)
                    .latitude(proposal.getLatitude())
                    .longitude(proposal.getLongitude())
                    .description(proposal.getUserNotes())
                    .address(address)
                    .city(city)
                    .active(false) // Created but not active until proposal is approved
                    .build();

            Monument savedMonument = monumentService.create(newMonument);
            proposal.setExistingMonument(savedMonument);
            proposalRepo.save(proposal);
            log.info("Created new monument with ID: {}", savedMonument.getId());
        }
    }
}
