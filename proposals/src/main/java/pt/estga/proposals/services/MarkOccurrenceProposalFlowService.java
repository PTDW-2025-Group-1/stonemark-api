package pt.estga.proposals.services;

import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.io.IOException;

public interface MarkOccurrenceProposalFlowService {

    MarkOccurrenceProposal initiate(Long userId, byte[] photoData, String filename) throws IOException;

    MarkOccurrenceProposal updatePhoto(Long proposalId, byte[] photoData, String filename) throws IOException;

    MarkOccurrenceProposal selectMonument(Long proposalId, Long existingMonumentId);

    MarkOccurrenceProposal proposeMonument(Long proposalId, String title, Double latitude, Double longitude);

    MarkOccurrenceProposal selectMark(Long proposalId, Long existingMarkId);

    MarkOccurrenceProposal proposeMark(Long proposalId, String description);

    MarkOccurrenceProposal requestNewMark(Long proposalId);

    MarkOccurrenceProposal requestNewMonument(Long proposalId);

    MarkOccurrenceProposal confirmMonumentLocation(Long proposalId, boolean confirmed);

    MarkOccurrenceProposal addNotesToProposal(Long proposalId, String notes);

    MarkOccurrenceProposal addLocationToProposal(Long proposalId, Double latitude, Double longitude);

}
