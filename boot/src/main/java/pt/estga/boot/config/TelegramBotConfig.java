package pt.estga.boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.estga.bots.telegram.StonemarkTelegramBot;
import pt.estga.bots.telegram.TelegramBotCommandService;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;
import pt.estga.proposals.services.ProposalSubmissionService;

@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.webhook-path}")
    private String webhookPath;

    @Bean
    public TelegramBotCommandService telegramBotCommandService(MarkOccurrenceProposalFlowService proposalFlowService, ProposalSubmissionService proposalSubmissionService) {
        return new TelegramBotCommandService(proposalFlowService, proposalSubmissionService);
    }

    @Bean
    public StonemarkTelegramBot stonemarkTelegramBot(TelegramBotCommandService commandService) {
        return new StonemarkTelegramBot(botUsername, botToken, webhookPath, commandService);
    }
}
