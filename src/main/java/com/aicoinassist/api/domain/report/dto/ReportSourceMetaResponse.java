package com.aicoinassist.api.domain.report.dto;

public record ReportSourceMetaResponse(
	String sourceDataVersion,
	String analysisEngineVersion,
	String llmProvider,
	String llmModel,
	String generationStatus,
	boolean fallbackUsed,
	String promptTemplateVersion,
	String inputSchemaVersion,
	String outputSchemaVersion,
	Long sharedContextId,
	String sharedContextVersion,
	boolean sharedContextUsed
) {
}
