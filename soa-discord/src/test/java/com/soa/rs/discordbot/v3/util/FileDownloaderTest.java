package com.soa.rs.discordbot.v3.util;

import org.junit.Assert;
import org.junit.Test;

import discord4j.core.object.util.Image;

public class FileDownloaderTest {

	@Test
	public void testPng() {
		try {
			Assert.assertEquals(Image.Format.PNG, FileDownloader.getFormatForProvidedURL("http://image.com/image.png"));
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testJpg() {
		try {
			Assert.assertEquals(Image.Format.JPEG,
					FileDownloader.getFormatForProvidedURL("http://image.com/image.jpg"));
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testJpeg() {
		try {
			Assert.assertEquals(Image.Format.JPEG,
					FileDownloader.getFormatForProvidedURL("http://image.com/image.jpeg"));
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testGif() {
		try {
			Assert.assertEquals(Image.Format.GIF, FileDownloader.getFormatForProvidedURL("http://image.com/image.gif"));
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testUnknownExtension() {
		try {
			FileDownloader.getFormatForProvidedURL("http://image.com/image.unk");
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals("Unable to determine format of [unk]", e.getMessage());
		}
	}

	@Test
	public void testNoExtension() {
		try {
			FileDownloader.getFormatForProvidedURL("");
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals("URL did not contain an extension", e.getMessage());
		}
	}

	@Test
	public void testCreateUrl() {
		FileDownloader downloader = new FileDownloader();
		try {
			Assert.assertEquals("http://image.com/image.jpg",
					FileDownloader.createUrl("http://image.com/image.jpg").toExternalForm());
		} catch (Exception e) {
			Assert.fail();
		}
	}
}
