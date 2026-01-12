package com.code81.tmo.liferay.rest.internal.resource.v1_0;

import com.code81.tmo.liferay.rest.dto.v1_0.News;
import com.code81.tmo.liferay.rest.dto.v1_0.NewsResponse;
import com.code81.tmo.liferay.rest.internal.utils.GeneralListing;
import com.code81.tmo.liferay.rest.internal.utils.GeneralValidation;
import com.code81.tmo.liferay.rest.resource.v1_0.NewsResource;

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
	properties = "OSGI-INF/liferay/rest/v1_0/news.properties",
	scope = ServiceScope.PROTOTYPE, service = NewsResource.class
)
public class NewsResourceImpl extends BaseNewsResourceImpl {

	private static final Log logger = LogFactoryUtil.getLog(NewsResourceImpl.class);

	private static final Long NEWS_STRUCTURE_ID = 34176L;

	@Reference
	private GeneralListing generalListing;

	@Override
	public NewsResponse getNews(String keyword, String date, String page, String size) throws Exception {
		Locale locale = contextHttpServletRequest.getLocale();
		String languageId = locale.toString();

		requestValidation(date, page, size);

		int pageNumber = (page != null && Integer.parseInt(page) > 0) ? Integer.parseInt(page) : 1;
		int pageSize   = (size != null && Integer.parseInt(size) > 0) ? Integer.parseInt(size) : 10;

		SearchResponse response = generalListing.getSearchHits(NEWS_STRUCTURE_ID, "news", HtmlUtil.escape(keyword), date, null, pageSize, pageNumber);

		SearchHits hits = response.getSearchHits();
		List<SearchHit> searchHits = hits.getSearchHits();

		List<News> newsList = new ArrayList<>();

		for (SearchHit hit : searchHits) {
			Document document = hit.getDocument();

			String docTitle = document.getString("title_" + languageId) != null ? document.getString("title_" + languageId) : document.getString("title_en_US");
			String docDescription = document.getString("description" + languageId) != null ? document.getString("description_" + languageId) : document.getString("description_en_US");

			long docId = document.getLong("entryClassPK");
			JournalArticle article = JournalArticleLocalServiceUtil.getLatestArticle(docId);

			newsList.add(mapArticleToNews(article, docTitle, docDescription, languageId));
		}

		int totalHits = response.getTotalHits();
		int totalPages = (int) Math.ceil((double) totalHits / pageSize);

		NewsResponse newsResponse = new NewsResponse();
		newsResponse.setNewses(newsList.toArray(new News[0]));
		newsResponse.setPage(pageNumber);
		newsResponse.setSize(pageSize);
		newsResponse.setTotalCount((long) totalHits);
		newsResponse.setTotalPages(totalPages);

		return newsResponse;
	}

	private News mapArticleToNews(JournalArticle article, String title, String description, String languageId) {
		try {
			News news = new News();
			news.setTitle(title);
			news.setDescription(description);
			String bannerImage = "";

			String localizedContent = article.getContentByLocale(languageId);
			com.liferay.portal.kernel.xml.Document contentDocument = SAXReaderUtil.read(localizedContent);

			Node bannerNode = contentDocument.selectSingleNode("/root/dynamic-element[@field-reference='NewsImages']/dynamic-content");
			if (bannerNode != null && Validator.isNotNull(bannerNode.getText())) {
				bannerImage = bannerNode.getText();
				JSONObject bannerImageObject = JSONFactoryUtil.createJSONObject(bannerImage);
				if (bannerImageObject.getLong("fileEntryId") == 0)
					bannerImage = "";
				else {
					bannerImage = "/documents/" + bannerImageObject.getLong("groupId") + "/" + bannerImageObject.getLong("fileEntryId") + "/" + bannerImageObject.getString("title") + "/" + bannerImageObject.getString("uuid");

				}
			}
			news.setImageUrl(PortalUtil.getPortalURL(contextHttpServletRequest) + bannerImage);
			news.setDate(article.getCreateDate().toString());

			return news;
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