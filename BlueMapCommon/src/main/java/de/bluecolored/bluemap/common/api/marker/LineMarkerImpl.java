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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.Line;
import de.bluecolored.bluemap.api.marker.LineMarker;
import ninja.leaping.configurate.ConfigurationNode;

import java.awt.*;
import java.util.List;

public class LineMarkerImpl extends ObjectMarkerImpl implements LineMarker {
	public static final String MARKER_TYPE = "line";

	private Line line;
	private boolean depthTest;
	private int lineWidth;
	private Color lineColor;

	private boolean hasUnsavedChanges;

	public LineMarkerImpl(String id, BlueMapMap map, Vector3d position, Line line) {
		super(id, map, position);

		Preconditions.checkNotNull(line);
		
		this.line = line;
		this.lineWidth = 2;
		this.lineColor = new Color(255, 0, 0, 200);

		this.hasUnsavedChanges = true;
	}
	
	@Override
	public String getType() {
		return MARKER_TYPE;
	}

	@Override
	public Line getLine() {
		return line;
	}

	@Override
	public synchronized void setLine(Line line) {
		Preconditions.checkNotNull(line);
		
		this.line = line;
		this.hasUnsavedChanges = true;
	}
	
	@Override
	public boolean isDepthTestEnabled() {
		return this.depthTest;
	}

	@Override
	public void setDepthTestEnabled(boolean enabled) {
		this.depthTest = enabled;
		this.hasUnsavedChanges = true;
	}

	@Override
	public int getLineWidth() {
		return lineWidth;
	}

	@Override
	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
		this.hasUnsavedChanges = true;
	}

	@Override
	public Color getLineColor() {
		return this.lineColor;
	}

	@Override
	public synchronized void setLineColor(Color color) {
		Preconditions.checkNotNull(color);
		
		this.lineColor = color;
		this.hasUnsavedChanges = true;
	}
	
	@Override
	public void load(BlueMapAPI api, ConfigurationNode markerNode, boolean overwriteChanges) throws MarkerFileFormatException {
		super.load(api, markerNode, overwriteChanges);

		if (!overwriteChanges && hasUnsavedChanges) return;
		this.hasUnsavedChanges = false;
		
		this.line = readLine(markerNode.getNode("line"));
		this.depthTest = markerNode.getNode("depthTest").getBoolean(true);
		this.lineWidth = markerNode.getNode("lineWidth").getInt(2);
		this.lineColor = readColor(markerNode.getNode("lineColor"));
	}
	
	@Override
	public void save(ConfigurationNode markerNode) {
		super.save(markerNode);

		writeLine(markerNode.getNode("line"), this.line);
		markerNode.getNode("depthTest").setValue(this.depthTest);
		markerNode.getNode("lineWidth").setValue(this.lineWidth);
		writeColor(markerNode.getNode("lineColor"), this.lineColor);

		
		hasUnsavedChanges = false;
	}
	
	private Line readLine(ConfigurationNode node) throws MarkerFileFormatException {
		List<? extends ConfigurationNode> posNodes = node.getChildrenList();
		
		if (posNodes.size() < 3) throw new MarkerFileFormatException("Failed to read line: point-list has fewer than 2 entries!");
		
		Vector3d[] positions = new Vector3d[posNodes.size()];
		for (int i = 0; i < positions.length; i++) {
			positions[i] = readLinePos(posNodes.get(i));
		}
		
		return new Line(positions);
	}
	
	private static Vector3d readLinePos(ConfigurationNode node) throws MarkerFileFormatException {
		ConfigurationNode nx, ny, nz;
		nx = node.getNode("x");
		ny = node.getNode("y");
		nz = node.getNode("z");
		
		if (nx.isVirtual() || ny.isVirtual() || nz.isVirtual()) throw new MarkerFileFormatException("Failed to read line position: Node x, y or z is not set!");
		
		return new Vector3d(
				nx.getDouble(),
				ny.getDouble(),
				nz.getDouble()
			);
	}
	
	private static Color readColor(ConfigurationNode node) throws MarkerFileFormatException {
		ConfigurationNode nr, ng, nb, na;
		nr = node.getNode("r");
		ng = node.getNode("g");
		nb = node.getNode("b");
		na = node.getNode("a");
		
		if (nr.isVirtual() || ng.isVirtual() || nb.isVirtual()) throw new MarkerFileFormatException("Failed to read color: Node r,g or b is not set!");
		
		float alpha = na.getFloat(1);
		if (alpha < 0 || alpha > 1) throw new MarkerFileFormatException("Failed to read color: alpha value out of range (0-1)!");
		
		try {
			return new Color(nr.getInt(), ng.getInt(), nb.getInt(), (int)(alpha * 255));
		} catch (IllegalArgumentException ex) {
			throw new MarkerFileFormatException("Failed to read color: " + ex.getMessage(), ex);
		}
	}
	
	private static void writeLine(ConfigurationNode node, Line line) {
		for (int i = 0; i < line.getPointCount(); i++) {
			ConfigurationNode pointNode = node.appendListNode();
			Vector3d point = line.getPoint(i);
			pointNode.getNode("x").setValue(Math.round(point.getX() * 1000d) / 1000d);
			pointNode.getNode("y").setValue(Math.round(point.getY() * 1000d) / 1000d);
			pointNode.getNode("z").setValue(Math.round(point.getZ() * 1000d) / 1000d);
		}
	}
	
	private static void writeColor(ConfigurationNode node, Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		float a = color.getAlpha() / 255f;
		
		node.getNode("r").setValue(r);
		node.getNode("g").setValue(g);
		node.getNode("b").setValue(b);
		node.getNode("a").setValue(a);
	}

}
