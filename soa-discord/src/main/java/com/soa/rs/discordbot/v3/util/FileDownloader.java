package com.soa.rs.discordbot.v3.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import discord4j.rest.util.Image;

/**
 * Utility class to generate an InputStream from an external file on the internet
 */
public class FileDownloader {

	/**
	 * Get an input stream of the given URL's contents
	 * @param url String form of URL to download
	 * @return InputStream containing the URL's data
	 * @throws Exception If an error occurs when getting the URL's content
	 */
	public static InputStream downloadFile(String url) throws Exception
	{
		if(!url.startsWith("http://") && !url.startsWith("https://"))
		{
			throw new MalformedURLException("Unable to handle this kind of URL.");
		}
		HttpURLConnection connection;
		if(url.startsWith("https://"))
		{
			connection = (HttpsURLConnection) createUrl(url).openConnection();
		}
		else
		{
			connection = (HttpURLConnection) createUrl(url).openConnection();
		}
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
		connection.setReadTimeout(10 * 1000);
		connection.connect();

		return connection.getInputStream();
	}

	/**
	 * Generates a URL object with the given URL
	 * @param url String form of the URL
	 * @return URL object
	 * @throws Exception If there is an error creating the URL
	 */
	public static URL createUrl(String url) throws Exception
	{
		return new URL(url);
	}

	public static Image.Format getFormatForProvidedURL(String url) throws Exception
	{
		Image.Format format;
		String imageType = url.substring(url.lastIndexOf(".") + 1);
		if(!imageType.trim().isEmpty()) {

			switch (imageType.toLowerCase()) {
			case "png":
				format = Image.Format.PNG;
				break;
			case "jpg":
			case "jpeg":
				format = Image.Format.JPEG;
				break;
			case "gif":
				format = Image.Format.GIF;
				break;
			default:
				throw new Exception("Unable to determine format of [" + imageType + "]");

			}
			return format;
		}
		else
		{
			throw new Exception("URL did not contain an extension");
		}
	}

}
