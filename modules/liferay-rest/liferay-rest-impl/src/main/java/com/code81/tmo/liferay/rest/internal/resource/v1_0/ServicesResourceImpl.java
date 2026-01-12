package com.code81.tmo.liferay.rest.internal.resource.v1_0;


import com.code81.tmo.liferay.rest.dto.v1_0.Service;
import com.code81.tmo.liferay.rest.dto.v1_0.ServicesResponse;
import com.code81.tmo.liferay.rest.internal.utils.GeneralListing;
import com.code81.tmo.liferay.rest.internal.utils.GeneralValidation;
import com.code81.tmo.liferay.rest.resource.v1_0.ServicesResource;

import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.searcher.SearchResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Mahmoud.Khalil
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/services.properties",
	scope = ServiceScope.PROTOTYPE, service = ServicesResource.class
)
public class ServicesResourceImpl extends BaseServicesResourceImpl {

	private final static Log logger = LogFactoryUtil.getLog(ServicesResourceImpl.class);

	private final static Long SERVICES_STRUCTURE_ID = 34198L;

	@Reference
	private GeneralListing generalListing;

	@Override
	public ServicesResponse getServices(String keyword, String source, String date, String page, String size) throws Exception {
		Locale locale = contextHttpServletRequest.getLocale();
		String languageId = locale.toString();

		requestValidation(date, page, size);

		int pageNumber = (page != null && Integer.parseInt(page) > 0) ? Integer.parseInt(page) : 1;
		int pageSize   = (size != null && Integer.parseInt(size) > 0) ? Integer.parseInt(size) : 10;

		SearchResponse response = generalListing.getSearchHits(SERVICES_STRUCTURE_ID, "services", HtmlUtil.escape(keyword), date, source, pageSize, pageNumber);

		SearchHits hits = response.getSearchHits();
		List<SearchHit> searchHits = hits.getSearchHits();

		List<Service> services = new ArrayList<>();

		for (SearchHit hit : searchHits) {
			Document document = hit.getDocument();

			String docTitle = document.getString("title_" + languageId) != null ? document.getString("title_" + languageId) : document.getString("title_en_US");
			String docDescription = document.getString("description" + languageId) != null ? document.getString("description_" + languageId) : document.getString("description_en_US");

			long docId = document.getLong("entryClassPK");
			JournalArticle article = JournalArticleLocalServiceUtil.getLatestArticle(docId);

			services.add(mapArticleToService(article, docTitle, docDescription, languageId));
		}

		int totalHits = response.getTotalHits();
		int totalPages = (int) Math.ceil((double) totalHits / pageSize);

		ServicesResponse servicesResponse = new ServicesResponse();
		servicesResponse.setServices(services.toArray(new Service[0]));
		servicesResponse.setPage(pageNumber);
		servicesResponse.setSize(pageSize);
		servicesResponse.setTotalCount((long) totalHits);
		servicesResponse.setTotalPages(totalPages);

		return servicesResponse;
	}

	private Service mapArticleToService(JournalArticle article, String title, String description, String languageId) {
		try {
			Service service = new Service();
			service.setTitle(title);
			service.setDescription(description);
			String bannerImage = "";

			String localizedContent = article.getContentByLocale(languageId);
			com.liferay.portal.kernel.xml.Document contentDocument = SAXReaderUtil.read(localizedContent);

			Node bannerNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='ServiceIcon']/dynamic-content");
			if (bannerNode != null && Validator.isNotNull(bannerNode.getText())) {
				bannerImage = bannerNode.getText();
				JSONObject bannerImageObject = JSONFactoryUtil.createJSONObject(bannerImage);
				if (bannerImageObject.getLong("fileEntryId") == 0) {
					bannerImage = "";
				}
				else {
					bannerImage = "/documents/" + bannerImageObject.getLong("groupId") + "/" + bannerImageObject.getLong("fileEntryId") + "/" + bannerImageObject.getString("title") + "/" + bannerImageObject.getString("uuid");

				}
			}
			service.setImageUrl(PortalUtil.getPortalURL(contextHttpServletRequest) + bannerImage);

			Node sourceNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='ServiceSource']/dynamic-content");
			if (sourceNode != null) {
				logger.info("ServiceSource: " + sourceNode.getText());
				service.setSource(getSourceValue(sourceNode.getText(), article, contextHttpServletRequest.getLocale()));
			}

			Node briefNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='ServiceBrief']/dynamic-content");
			if (briefNode != null) {
				logger.info("ServiceBrief: " + briefNode.getText());
				service.setBrief(briefNode.getText());
			}

			Node codeNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='ServiceCode']/dynamic-content");
			if (codeNode != null) {
				logger.info("ServiceCode: " + codeNode.getText());
				service.setCode(codeNode.getText());
			}

			Node procedureNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='ServiceProcedures']/dynamic-content");
			if (procedureNode != null) {
				logger.info("ServiceProcedures: " + procedureNode.getText());
				service.setProcedure(procedureNode.getText());
			}

			Node externalUrlNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='ExternalURL']/dynamic-content");
			if (externalUrlNode != null) {
				logger.info("ExternalURL: " + externalUrlNode.getText());
				service.setExternalUrl(externalUrlNode.getText());
			}

			List<String> documentsURLs = new ArrayList<>();
			List<Node> documentNodes = contentDocument.selectNodes("/root/dynamic-element[@field-reference='RequiredDocuments']/dynamic-content");
			for (Node documentNode : documentNodes) {
				if (documentNode != null) {
					String serviceDocument = documentNode.getText();
					JSONObject serviceDocumentObject = JSONFactoryUtil.createJSONObject(serviceDocument);
					if (serviceDocumentObject.getLong("fileEntryId") == 0) {
						serviceDocument = "";
					}
					else {
						serviceDocument = "/documents/" + serviceDocumentObject.getLong("groupId") + "/" + serviceDocumentObject.getLong("fileEntryId") + "/" + serviceDocumentObject.getString("title") + "/" + serviceDocumentObject.getString("uuid");
					}
					logger.info("RequiredDocuments: " + documentNode.getText());
					documentsURLs.add(PortalUtil.getPortalURL(contextHttpServletRequest) + serviceDocument);
				}
			}
			service.setDocuments(documentsURLs.toArray(new String[0]));


			Node termsAndCoditionsNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='TermsAndConditions']/dynamic-content");
			if (termsAndCoditionsNode != null) {
				logger.info("TermsAndConditions: " + termsAndCoditionsNode.getText());
				service.setTermsAndConditions(termsAndCoditionsNode.getText());
			}

			Node outcomeNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='ServiceOutcome']/dynamic-content");
			if (outcomeNode != null) {
				logger.info("ServiceOutcome: " + outcomeNode.getText());
				service.setOutcome(outcomeNode.getText());
			}

			Node expectedResponseTimeNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='ExpectedResponseTime']/dynamic-content");
			if (expectedResponseTimeNode != null) {
				logger.info("ExpectedResponseTime: " + expectedResponseTimeNode.getText());
				service.setExpecterResponseTime(Integer.valueOf(expectedResponseTimeNode.getText()));
			}


			service.setDate(article.getCreateDate().toString());

			return service;
		} catch (Exception e) {
			logger.error("Error mapping article to event: " + e.getMessage(), e);
			return null;
		}
	}

	private void requestValidation(String date, String page, String size) {
		if(date != null && !GeneralValidation.isValidIsoDate(date)) {
			throw new BadRequestException("Invalid date format, format: yyyy-MM-dd");
		}

		if(page != null && !GeneralValidation.isValidNumber(String.valueOf(page))) {
			throw new BadRequestException("Invalid page number");
		}

		if (size != null && !GeneralValidation.isValidNumber(String.valueOf(size))) {
			throw new BadRequestException("Invalid page size");
		}
	}

	private String getSourceValue(String optionId, JournalArticle article, Locale locale) {

		try {
			DDMStructure ddmStructure =
					DDMStructureLocalServiceUtil.getDDMStructure(
							article.getDDMStructureId()
					);

			DDMFormField sourceField = null;

			for (DDMFormField field : ddmStructure.getDDMFormFields(true)) {
				if ("ServiceSource".equals(field.getFieldReference())) {
					sourceField = field;
					break;
				}
			}

			if (sourceField == null) {
				throw new IllegalStateException(
						"DDM field with fieldReference 'ServiceSource' not found"
				);
			}

			return sourceField
					.getDDMFormFieldOptions()
					.getOptionLabels(optionId)
					.getString(locale);

		} catch (Exception e) {
			logger.error("Error mapping article to service: " + e.getMessage(), e);
			return null;
		}
	}

}