package com.nector.auth.security.util;

import jakarta.servlet.http.HttpServletRequest;

public class DeviceUtils {

	private DeviceUtils() {
	}

	public static String resolveDeviceType(HttpServletRequest request) {

		String userAgent = request.getHeader("User-Agent");

		if (userAgent == null)
			return "UNKNOWN";

		userAgent = userAgent.toLowerCase();

		if (userAgent.contains("postman") || userAgent.contains("insomnia") || userAgent.contains("curl")
				|| userAgent.contains("swagger") || userAgent.contains("openapi")) {
			return "API";
		}

		if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
			return "MOBILE";
		}

		if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
			return "TABLET";
		}

		if (userAgent.contains("windows") || userAgent.contains("macintosh") || userAgent.contains("linux")) {
			return "WEB";
		}

		return "UNKNOWN";
	}
}
