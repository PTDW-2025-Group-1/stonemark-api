package pt.estga.stonemark.services.proposal;

import pt.estga.stonemark.entities.MediaFile;
import pt.estga.stonemark.entities.proposals.MarkOccurrenceProposal;

public interface MarkOccurrenceProposalSubmissionService {
    MarkOccurrenceProposal submitProposal(MediaFile photo, String monumentName, double latitude, double longitude);
}
