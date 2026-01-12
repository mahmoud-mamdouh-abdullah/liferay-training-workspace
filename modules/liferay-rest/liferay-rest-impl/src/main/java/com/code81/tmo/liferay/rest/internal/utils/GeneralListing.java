package com.code81.tmo.liferay.rest.internal.utils;

import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.searcher.SearchResponse;


public interface GeneralListing {

    BooleanQuery buildFilterQuery(String date, String structureKey, Long structureId, String source);

    SearchResponse getSearchHits(long structureId, String structureKey, String keyword, String date, String source, Integer size, Integer page);
}
