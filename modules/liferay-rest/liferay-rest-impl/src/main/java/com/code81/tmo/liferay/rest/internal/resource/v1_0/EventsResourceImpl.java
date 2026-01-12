package com.code81.tmo.liferay.rest.internal.resource.v1_0;

import com.code81.tmo.liferay.rest.dto.v1_0.Event;
import com.code81.tmo.liferay.rest.dto.v1_0.EventsResponse;
import com.code81.tmo.liferay.rest.internal.utils.GeneralListing;
import com.code81.tmo.liferay.rest.internal.utils.GeneralValidation;
import com.code81.tmo.liferay.rest.resource.v1_0.EventsResource;


import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.*;
import com.liferay.portal.search.searcher.SearchResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.ws.rs.BadRequestException;
import java.util.*;

/**
 * @author Mahmoud.Khalil
 */
@Component(
		properties = "OSGI-INF/liferay/rest/v1_0/events.properties",
		scope = ServiceScope.PROTOTYPE,
		service = EventsResource.class
)
public class EventsResourceImpl extends BaseEventsResourceImpl {

	private static final Log logger = LogFactoryUtil.getLog(EventsResourceImpl.class);

	private static final Long EVENTS_STRUCTURE_ID = 34184L;

	@Reference
	private GeneralListing generalListing;

	@Override
	public EventsResponse getEvents(String keyword, String date, String page, String size) throws Exception {
		Locale locale = contextHttpServletRequest.getLocale();
		String languageId = locale.toString();

		requestValidation(date, page, size);

		int pageNumber = (page != null && Integer.parseInt(page) > 0) ? Integer.parseInt(page) : 1;
		int pageSize   = (size != null && Integer.parseInt(size) > 0) ? Integer.parseInt(size) : 10;

		SearchResponse response = generalListing.getSearchHits(EVENTS_STRUCTURE_ID, "events", HtmlUtil.escape(keyword), date, null, pageSize, pageNumber);

		SearchHits hits = response.getSearchHits();
		List<SearchHit> searchHits = hits.getSearchHits();

		List<Event> events = new ArrayList<>();

		for (SearchHit hit : searchHits) {
			Document document = hit.getDocument();

			String docTitle = document.getString("title_" + languageId) != null ? document.getString("title_" + languageId) : document.getString("title_en_US");
			String docDescription = document.getString("description" + languageId) != null ? document.getString("description_" + languageId) : document.getString("description_en_US");

			long docId = document.getLong("entryClassPK");
			JournalArticle article = JournalArticleLocalServiceUtil.getLatestArticle(docId);

			events.add(mapArticleToEvent(article, docTitle, docDescription, languageId));
		}

		int totalHits = response.getTotalHits();
		int totalPages = (int) Math.ceil((double) totalHits / pageSize);

		EventsResponse eventsResponse = new EventsResponse();
		eventsResponse.setEvents(events.toArray(new Event[0]));
		eventsResponse.setPage(pageNumber);
		eventsResponse.setSize(pageSize);
		eventsResponse.setTotalCount((long) totalHits);
		eventsResponse.setTotalPages(totalPages);

		return eventsResponse;
	}

	private Event mapArticleToEvent(JournalArticle article, String title, String description, String languageId) {
		try {
			Event event = new Event();
			event.setTitle(title);
			event.setDescription(description);
			String bannerImage = "";
			String bannerImageAlt = "";

			String localizedContent = article.getContentByLocale(languageId);
			com.liferay.portal.kernel.xml.Document contentDocument = SAXReaderUtil.read(localizedContent);

			Node bannerNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='EventBanner']/dynamic-content");
			if (bannerNode != null && Validator.isNotNull(bannerNode.getText())) {
				bannerImage = bannerNode.getText();
				JSONObject bannerImageObject = JSONFactoryUtil.createJSONObject(bannerImage);
				if (bannerImageObject.getLong("fileEntryId") == 0)
					bannerImage = "";
				else {
					bannerImageAlt = bannerImageObject.getString("alt");
					bannerImageAlt = bannerImageAlt != null ? bannerImageAlt.replace("\"", "&quot;").replace("'", "&#39;") : "";
					bannerImage = "/documents/" + bannerImageObject.getLong("groupId") + "/" + bannerImageObject.getLong("fileEntryId") + "/" + bannerImageObject.getString("title") + "/" + bannerImageObject.getString("uuid");

				}
			}
			event.setImageUrl(PortalUtil.getPortalURL(contextHttpServletRequest) + bannerImage);

			Node dateNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='EventDate']/dynamic-content");
			if (dateNode != null) {
				logger.info("dateNode: " + dateNode.getText());
				event.setDate(dateNode.getText());
			}

			Node locationNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='EventLocation']/dynamic-content");
			if (locationNode != null) {
				event.setLocation(locationNode.getText());
			}

			Node timeFromNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='TimeFrom']/dynamic-content");
			if (timeFromNode != null) {
				event.setTimeFrom(timeFromNode.getText());
			}

			Node timeToNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='TimeTo']/dynamic-content");
			if (timeToNode != null) {
				event.setTimeTo(timeToNode.getText());
			}

			Node linkNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='EventLink']/dynamic-content");
			if (linkNode != null) {
				event.setLink(linkNode.getText());
			}
			return event;
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
}
