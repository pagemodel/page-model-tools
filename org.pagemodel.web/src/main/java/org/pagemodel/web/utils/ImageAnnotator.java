/*
 * Copyright 2021 Matthew Stevenson <pagemodel.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pagemodel.web.utils;

import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.pagemodel.core.testers.ComparableTester;
import org.pagemodel.core.testers.TestEvaluator;
import org.pagemodel.core.utils.ThrowingConsumer;
import org.pagemodel.core.utils.json.JsonObjectBuilder;
import org.pagemodel.web.WebTestContext;
import org.pagemodel.web.testers.BoundsTester;
import org.pagemodel.web.testers.PointTester;
import org.pagemodel.web.utils.Screenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.pagemodel.web.utils.Screenshot.SCREENSHOT_DEST;

/**
 * @author Matt Stevenson [matt@pagemodel.org]
 */
public class ImageAnnotator<R> {
	protected final R returnObj;
	protected final Callable<BufferedImage> ref;
	protected final WebTestContext testContext;
	private TestEvaluator testEvaluator;
	protected BufferedImage image = null;
	protected Graphics graphics = null;

	public ImageAnnotator(Callable<BufferedImage> ref, R returnObj, WebTestContext testContext, TestEvaluator testEvaluator) {
		this.ref = ref;
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.testEvaluator = testEvaluator;
	}

	protected Graphics callRef() {
		if(graphics != null){
			return graphics;
		}
		try {
			BufferedImage image = getImage();
			graphics = image == null ? null : image.getGraphics();
			return graphics;
		} catch (Exception ex) {
			return null;
		}
	}

	protected BufferedImage getImage() {
		if(image != null){
			return image;
		}
		try {
			image = ref.call();
			return image;
		} catch (Exception ex) {
			return null;
		}
	}

	protected TestEvaluator getEvaluator(){
		return testEvaluator;
	}

	public R storeValue(String key) {
		testContext.store(key, callRef());
		return returnObj;
	}

	public ImageAnnotator<R> setColor(Color color){
		callRef().setColor(color);
		return this;
	}

	public ImageAnnotator<R> setFont(Font font){
		callRef().setFont(font);
		return this;
	}

	public ImageAnnotator<R> drawRect(int x, int y, int width, int height){
		callRef().drawRect(x, y, width, height);
		return this;
	}

	public ImageAnnotator<R> drawRect(Rectangle rect){
		return drawRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public ImageAnnotator<R> drawRoundedRect(int x, int y, int width, int height, int arcWidth, int arcHeight){
		callRef().drawRoundRect(x, y, width, height, arcWidth, arcHeight);
		return this;
	}

	public ImageAnnotator<R> drawRoundedRect(Rectangle rect, int arcWidth, int arcHeight){
		return drawRoundedRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), arcWidth, arcHeight);
	}

	public ImageAnnotator<R> clip(int x, int y, int width, int height){
		callRef().clipRect(x, y, width, height);
		return this;
	}

	public ImageAnnotator<R> clip(Rectangle rect){
		return clip(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public ImageAnnotator<R> clear(int x, int y, int width, int height){
		callRef().clearRect(x, y, width, height);
		return this;
	}

	public ImageAnnotator<R> clear(Rectangle rect){
		return clear(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public ImageAnnotator<R> fillRect(int x, int y, int width, int height){
		callRef().fillRect(x, y, width, height);
		return this;
	}

	public ImageAnnotator<R> fillRect(Rectangle rect){
		return fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public ImageAnnotator<R> fillRoundedRect(int x, int y, int width, int height, int arcWidth, int arcHeight){
		callRef().fillRoundRect(x, y, width, height, arcWidth, arcHeight);
		return this;
	}

	public ImageAnnotator<R> fillRoundedRect(Rectangle rect, int arcWidth, int arcHeight){
		return fillRoundedRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), arcWidth, arcHeight);
	}

	public ImageAnnotator<R> drawText(String text, int x, int y){
		callRef().drawString(text, x, y);
		return this;
	}

	public ImageAnnotator<R> drawText(String text, Rectangle rect){
		return drawText(text, rect.getX(), rect.getY());
	}

	public ImageAnnotator<R> drawText(String text, Point point){
		return drawText(text, point.getX(), point.getY());
	}

	public ImageAnnotator<R> paint(ThrowingConsumer<Graphics,?> paintFunc){
		ThrowingConsumer.unchecked(paintFunc).accept(callRef());
		return this;
	}

	public R save(String filename){
		return save(filename, false);
	}

	public R save(String filename, boolean formatName){
		File destFolder = new File(SCREENSHOT_DEST);
		if (!destFolder.exists()) {
			getEvaluator().quiet().testRun(TestEvaluator.TEST_EXECUTE,
					"create directory", op -> op
							.addValue("value", destFolder.getAbsolutePath()),
					() -> destFolder.mkdirs(),
					null, null);
		}
		File screenshot;
		if(formatName){
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
			String date = simpleDateFormat.format(new Date());
			String formattedName = String.format("%03d_%s_%s.png", Screenshot.SCREENSHOT_NUMBER++, filename, date);
			screenshot = new File(destFolder, formattedName);
		}else{
			screenshot = new File(destFolder, filename+".png");
		}
		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
		String base64Encoded1;
		try {
			ImageIO.write(getImage(), "png", outBytes);
			base64Encoded1 = Base64.getEncoder().encodeToString(outBytes.toByteArray());
		}catch (IOException ex){
			base64Encoded1 = "";
		}
		final String base64Encoded = base64Encoded1;
		getEvaluator().testExecute("save image", op -> op
				.addValue("value", "file://" + screenshot.getAbsolutePath())
				.addValue("img-base64", base64Encoded),
				() -> ImageIO.write(getImage(), "png", screenshot),
				null, testContext);
		return returnObj;
	}
}
