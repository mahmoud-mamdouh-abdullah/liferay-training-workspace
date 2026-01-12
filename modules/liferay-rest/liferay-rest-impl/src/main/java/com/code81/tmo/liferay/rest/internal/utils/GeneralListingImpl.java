package com.code81.tmo.liferay.rest.internal.utils;

import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Component(immediate = true, service = GeneralListing.class)
public class GeneralListingImpl implements GeneralListing {

    private static final Log logger = LogFactoryUtil.getLog(GeneralListingImpl.class);

    @Reference
    private Queries _queries;

    @Reference
    private SearchRequestBuilderFactory _searchRequestBuilderFactory;

    @Reference
    private Searcher _searcher;

    @Override
    public BooleanQuery buildFilterQuery(String date, String structureKey, Long structureId, String source) {
        BooleanQuery booleanQuery = _queries.booleanQuery();

        if (date != null) {
            Date parsedDate = parseUiDate(date);
            if (parsedDate != null) {
                if ("events".equalsIgnoreCase(structureKey)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dateValue = sdf.format(parsedDate);

                    BooleanQuery nestedInner = _queries.booleanQuery();
                    nestedInner.addMustQueryClauses(
                            _queries.term("ddmFieldArray.ddmFieldName", "ddm__keyword__" + structureId + "__EventDate"),
                            _queries.term("ddmFieldArray.ddmFieldValueKeyword_String_sortable", dateValue)
                    );

                    Query nestedQuery = _queries.nested("ddmFieldArray", nestedInner);
                    booleanQuery.addMustQueryClauses(nestedQuery);
                } else {
                    String start = formatIndexDate(startOfDay(parsedDate));
                    String end = formatIndexDate(endOfDay(parsedDate));
                    Query dateRange = _queries.rangeTerm("displayDate", true, true, start, end);
                    booleanQuery.addMustQueryClauses(dateRange);
                }
            } else {
                logger.warn("Unable to parse dateParam: " + date);
            }
        }

        if("services".equalsIgnoreCase(structureKey) && source != null) {
            BooleanQuery nestedInner = _queries.booleanQuery();
            nestedInner.addMustQueryClauses(
                    _queries.term("ddmFieldArray.ddmFieldName", "ddm__keyword__" + structureId + "__ServiceSource"),
                    _queries.term("ddmFieldArray.ddmFieldValueKeyword_String_sortable", source.toLowerCase())
            );
            Query nestedQuery = _queries.nested("ddmFieldArray", nestedInner);
            booleanQuery.addMustQueryClauses(nestedQuery);
        }
        return booleanQuery;
    }

    @Override
    public SearchResponse getSearchHits(long structureId, String structureKey, String keyword, String date, String source, Integer pageSize, Integer pageNumber) {
        ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();

        int start = (pageNumber - 1) * pageSize;
        int end = start + pageSize;

        SearchRequestBuilder builder = _searchRequestBuilderFactory.builder();

        builder.withSearchContext(searchContext -> {
            searchContext.setCompanyId(serviceContext.getCompanyId());
            searchContext.setEntryClassNames(
                    new String[]{JournalArticle.class.getName()});
            searchContext.setClassTypeIds(new long[]{structureId});
            searchContext.setAttribute("head", Boolean.TRUE);
            searchContext.setKeywords(keyword);
            searchContext.setStart(start);
            searchContext.setEnd(end);
            searchContext.setAttribute(
                    Field.STATUS,
                    WorkflowConstants.STATUS_APPROVED
            );
        });

        builder.emptySearchEnabled(true);

        Query filterQuery = this.buildFilterQuery(date, structureKey, structureId, source);
        if (filterQuery != null) {
            builder.postFilterQuery(filterQuery);
        }
        logger.info("filterQuery: " + filterQuery);

        return _searcher.search(builder.build());
    }

    private Date parseUiDate(String uiDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            return sdf.parse(uiDate);
        } catch (ParseException e) {
            return null;
        }
    }

    private Date startOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private String formatIndexDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
