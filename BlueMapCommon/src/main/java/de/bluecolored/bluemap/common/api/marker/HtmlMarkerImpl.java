/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.common.api.marker;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.HtmlMarker;
import ninja.leaping.configurate.ConfigurationNode;

public class HtmlMarkerImpl extends MarkerImpl implements HtmlMarker {
	public static final String MARKER_TYPE = "html";

	private String html;
	private Vector2i anchor;

	private boolean hasUnsavedChanges;

	public HtmlMarkerImpl(String id, BlueMapMap map, Vector3d position, String html) {
		super(id, map, position);
		
		this.html = html;
		this.anchor = new Vector2i(25, 45);
		
		this.hasUnsavedChanges = true;
	}
	
	@Override
	public String getType() {
		return MARKER_TYPE;
	}

	@Override
	public Vector2i getAnchor() {
		return anchor;
	}

	@Override
	public void setAnchor(Vector2i anchor) {
		this.anchor = anchor;
		this.hasUnsavedChanges = true;
	}

	@Override
	public String getHtml() {
		return html;
	}

	@Override
	public synchronized void setHtml(String html) {
		this.html = html;
		this.hasUnsavedChanges = true;
	}
	
	@Override
	public synchronized void load(BlueMapAPI api, ConfigurationNode markerNode, boolean overwriteChanges) throws MarkerFileFormatException {
		super.load(api, markerNode, overwriteChanges);

		if (!overwriteChanges && hasUnsavedChanges) return;
		this.hasUnsavedChanges = false;

		this.html = markerNode.getNode("html").getString("");
		this.anchor = readAnchor(markerNode.getNode("anchor"));
	}
	
	@Override
	public synchronized void save(ConfigurationNode markerNode) {
		super.save(markerNode);
		
		markerNode.getNode("html").setValue(this.html);
		writeAnchor(markerNode.getNode("anchor"), this.anchor);
		
		hasUnsavedChanges = false;
	}
	
	private static Vector2i readAnchor(ConfigurationNode node) {
		return new Vector2i(
				node.getNode("x").getInt(0),
				node.getNode("y").getInt(0)
			);
	}
	
	private static void writeAnchor(ConfigurationNode node, Vector2i anchor) {
		node.getNode("x").setValue(anchor.getX());
		node.getNode("y").setValue(anchor.getY());
	}

}
