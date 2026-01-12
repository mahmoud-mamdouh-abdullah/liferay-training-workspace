package com.code81.tmo.liferay.rest.client.resource.v1_0;

import com.code81.tmo.liferay.rest.client.dto.v1_0.ServicesResponse;
import com.code81.tmo.liferay.rest.client.http.HttpInvoker;
import com.code81.tmo.liferay.rest.client.problem.Problem;
import com.code81.tmo.liferay.rest.client.serdes.v1_0.ServicesResponseSerDes;

import java.net.URL;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Generated;

/**
 * @author Mahmoud.Khalil
 * @generated
 */
@Generated("")
public interface ServicesResource {

	public static Builder builder() {
		return new Builder();
	}

	public ServicesResponse getServices(
			String keyword, String source, String date, String page,
			String size)
		throws Exception;

	public HttpInvoker.HttpResponse getServicesHttpResponse(
			String keyword, String source, String date, String page,
			String size)
		throws Exception;

	public static class Builder {

		public Builder authentication(String login, String password) {
			_login = login;
			_password = password;

			return this;
		}

		public Builder bearerToken(String token) {
			return header("Authorization", "Bearer " + token);
		}

		public ServicesResource build() {
			return new ServicesResourceImpl(this);
		}

		public Builder contextPath(String contextPath) {
			_contextPath = contextPath;

			return this;
		}

		public Builder endpoint(String address, String scheme) {
			String[] addressParts = address.split(":");

			String host = addressParts[0];

			int port = 443;

			if (addressParts.length > 1) {
				String portString = addressParts[1];

				try {
					port = Integer.parseInt(portString);
				}
				catch (NumberFormatException numberFormatException) {
					throw new IllegalArgumentException(
						"Unable to parse port from " + portString);
				}
			}

			return endpoint(host, port, scheme);
		}

		public Builder endpoint(String host, int port, String scheme) {
			_host = host;
			_port = port;
			_scheme = scheme;

			return this;
		}

		public Builder endpoint(URL url) {
			return endpoint(url.getHost(), url.getPort(), url.getProtocol());
		}

		public Builder header(String key, String value) {
			_headers.put(key, value);

			return this;
		}

		public Builder locale(Locale locale) {
			_locale = locale;

			return this;
		}

		public Builder parameter(String key, String value) {
			_parameters.put(key, value);

			return this;
		}

		public Builder parameters(String... parameters) {
			if ((parameters.length % 2) != 0) {
				throw new IllegalArgumentException(
					"Parameters length is not an even number");
			}

			for (int i = 0; i < parameters.length; i += 2) {
				String parameterName = String.valueOf(parameters[i]);
				String parameterValue = String.valueOf(parameters[i + 1]);

				_parameters.put(parameterName, parameterValue);
			}

			return this;
		}

		private Builder() {
		}

		private String _contextPath = "";
		private Map<String, String> _headers = new LinkedHashMap<>();
		private String _host = "localhost";
		private Locale _locale;
		private String _login;
		private String _password;
		private Map<String, String> _parameters = new LinkedHashMap<>();
		private int _port = 8080;
		private String _scheme = "http";

	}

	public static class ServicesResourceImpl implements ServicesResource {

		public ServicesResponse getServices(
				String keyword, String source, String date, String page,
				String size)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse = getServicesHttpResponse(
				keyword, source, date, page, size);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				Problem.ProblemException problemException = null;

				if (Objects.equals(
						httpResponse.getContentType(), "application/json")) {

					problemException = new Problem.ProblemException(
						Problem.toDTO(content));
				}
				else {
					_logger.log(
						Level.WARNING,
						"Unable to process content type: " +
							httpResponse.getContentType());

					Problem problem = new Problem();

					problem.setStatus(
						String.valueOf(httpResponse.getStatusCode()));

					problemException = new Problem.ProblemException(problem);
				}

				throw problemException;
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}

			try {
				return ServicesResponseSerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse getServicesHttpResponse(
				String keyword, String source, String date, String page,
				String size)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.GET);

			if (keyword != null) {
				httpInvoker.parameter("keyword", String.valueOf(keyword));
			}

			if (source != null) {
				httpInvoker.parameter("source", String.valueOf(source));
			}

			if (date != null) {
				httpInvoker.parameter("date", String.valueOf(date));
			}

			if (page != null) {
				httpInvoker.parameter("page", String.valueOf(page));
			}

			if (size != null) {
				httpInvoker.parameter("size", String.valueOf(size));
			}

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + _builder._contextPath +
						"/o/liferay-rest/v1.0/services");

			if ((_builder._login != null) && (_builder._password != null)) {
				httpInvoker.userNameAndPassword(
					_builder._login + ":" + _builder._password);
			}

			return httpInvoker.invoke();
		}

		private ServicesResourceImpl(Builder builder) {
			_builder = builder;
		}

		private static final Logger _logger = Logger.getLogger(
			ServicesResource.class.getName());

		private Builder _builder;

	}

}