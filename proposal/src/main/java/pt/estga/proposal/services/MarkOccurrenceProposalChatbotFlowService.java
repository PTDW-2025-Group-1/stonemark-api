package pt.estga.proposal.services;

import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.user.entities.User;

import java.io.IOException;
import java.util.List;

public interface MarkOccurrenceProposalChatbotFlowService {

    MarkOccurrenceProposal startProposal(User user);

    void addPhoto(MarkOccurrenceProposal proposal, byte[] photoData, String filename) throws IOException;

    void addLocation(MarkOccurrenceProposal proposal, Double latitude, Double longitude);

    List<Monument> suggestMonuments(MarkOccurrenceProposal proposal);

    void selectMonument(MarkOccurrenceProposal proposal, Long monumentId);

    void setNewMonumentName(MarkOccurrenceProposal proposal, String name);

    List<Mark> suggestMarks(MarkOccurrenceProposal proposal);

    void selectMark(MarkOccurrenceProposal proposal, Long markId);

    void indicateNewMark(MarkOccurrenceProposal proposal);

    void addNotes(MarkOccurrenceProposal proposal, String notes);

}
