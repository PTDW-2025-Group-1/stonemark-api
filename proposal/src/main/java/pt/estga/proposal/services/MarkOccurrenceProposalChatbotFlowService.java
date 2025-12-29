package pt.estga.proposal.services;

import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

import java.io.IOException;
import java.util.List;

public interface MarkOccurrenceProposalChatbotFlowService {

    MarkOccurrenceProposal startProposal(Long userId);

    void addPhotoAndAnalyze(Long proposalId, byte[] photoData, String filename) throws IOException;

    void addLocation(Long proposalId, Double latitude, Double longitude);

    List<Monument> suggestMonuments(Long proposalId);

    void selectMonument(Long proposalId, Long monumentId);

    void setNewMonumentName(Long proposalId, String name);

    List<Mark> suggestMarks(Long proposalId);

    void selectMark(Long proposalId, Long markId);

    void indicateNewMark(Long proposalId);

    void addNotes(Long proposalId, String notes);

    MarkOccurrenceProposal getProposal(Long proposalId);

}
