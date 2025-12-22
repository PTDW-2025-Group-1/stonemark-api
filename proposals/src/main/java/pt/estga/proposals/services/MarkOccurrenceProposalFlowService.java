package pt.estga.proposals.services;

import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.io.IOException;
import java.util.List;

public interface MarkOccurrenceProposalFlowService {

    MarkOccurrenceProposal initiate(Long userId, byte[] photoData, String filename, Double latitude, Double longitude) throws IOException;

    MarkOccurrenceProposal analyzeMedia(Long proposalId) throws IOException;

    MarkOccurrenceProposal updatePhoto(Long proposalId, byte[] photoData, String filename) throws IOException;

    MarkOccurrenceProposal selectMonument(Long proposalId, Long existingMonumentId);

    MarkOccurrenceProposal proposeMonument(Long proposalId, String title, Double latitude, Double longitude);

    MarkOccurrenceProposal selectMark(Long proposalId, Long existingMarkId);

    MarkOccurrenceProposal proposeMark(Long proposalId, String description);

    MarkOccurrenceProposal addNotesToProposal(Long proposalId, String notes);

    MarkOccurrenceProposal addLocationToProposal(Long proposalId, Double latitude, Double longitude);

    MarkOccurrenceProposal getProposal(Long proposalId);

    List<String> getSuggestedMarkIds(Long proposalId);

    List<String> getSuggestedMonumentIds(Long proposalId);

}
