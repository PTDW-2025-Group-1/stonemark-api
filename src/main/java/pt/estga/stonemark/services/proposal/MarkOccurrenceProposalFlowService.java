package pt.estga.stonemark.services.proposal;

import org.springframework.web.multipart.MultipartFile;
import pt.estga.stonemark.dtos.proposal.ProposalStateDto;

import java.io.IOException;

public interface MarkOccurrenceProposalFlowService {
    ProposalStateDto initiateProposal(MultipartFile photo) throws IOException;
    ProposalStateDto updateMonument(Long proposalId, String monumentName, double latitude, double longitude);
    ProposalStateDto finalizeProposal(Long proposalId);
}
