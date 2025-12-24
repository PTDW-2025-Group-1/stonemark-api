package pt.estga.proposals.services;

import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.io.IOException;
import java.util.List;

public interface ChatbotProposalFlowService {

    MarkOccurrenceProposal startProposal(Long userId);

    MarkOccurrenceProposal addPhoto(Long proposalId, byte[] photoData, String filename) throws IOException;

    MarkOccurrenceProposal addLocation(Long proposalId, Double latitude, Double longitude);

    MarkOccurrenceProposal analyzePhoto(Long proposalId) throws IOException;

    List<String> getSuggestedMonumentIds(Long proposalId);

    MarkOccurrenceProposal selectMonument(Long proposalId, Long monumentId);

    MarkOccurrenceProposal createMonument(Long proposalId, String name);

    List<String> getSuggestedMarkIds(Long proposalId);

    MarkOccurrenceProposal selectMark(Long proposalId, Long markId);

    MarkOccurrenceProposal createMark(Long proposalId, String description);

    MarkOccurrenceProposal addNotes(Long proposalId, String notes);

    MarkOccurrenceProposal getProposal(Long proposalId);

}
