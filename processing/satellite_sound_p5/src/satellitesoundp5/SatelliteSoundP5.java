package satellitesoundp5;

import java.util.Collection;
import java.util.HashMap;

import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PFont;


public class SatelliteSoundP5 extends PApplet {

	OscP5 oscP5;
	
	HashMap<String, Satellite> satellites;
	
	PFont font;

	public static boolean speakerMode = false;

	
	static final private int NUM_SATELLITES = 20;

	public void setup() {
		size(600, 600);
		oscP5 = new OscP5(this, 12000);
		
		font = loadFont("Ubuntu-48.vlw");
		textFont(font, 12);
		
		satellites = new HashMap<String, Satellite>();
		for (int i = 0; i < NUM_SATELLITES ; i++) {
			String satKey = "/SAT"+(i+1);
			Satellite tmpSat = new Satellite(this, satKey);
			satellites.put(satKey, tmpSat);
		}
		
	}

	public void draw() {
		background(240);
		pushMatrix();
		rotate(-PI/2);
		translate(-width, 0);
		if(speakerMode) {
			drawSpeakers();
		} else {
			drawCircles();
		}
		
		Collection<Satellite> sats = satellites.values();
		for (Satellite s: sats) {
			s.draw();
		}
//		translate(width/2, height/2);
		popMatrix();
		fill(0);
		text("satellite sound", 10, 15);
		text("ptrv, 2011", width-60, height-10);
	}

	private void drawSpeakers() {
		stroke(100);
		strokeWeight(2);
		noFill();
		ellipse(width/2, height/2, 550, 550);
	}

	private void drawCircles() {
		stroke(100);
		strokeWeight(1);
		noFill();
		float eW = width/2;
		float eH = height/2;
		ellipse(eW, eH, 150, 150);
		ellipse(eW, eH, 300, 300);
		ellipse(eW, eH, 450, 450);
		ellipse(eW, eH, 600, 600);
	}

	public void oscEvent(OscMessage theOscMessage) {
		Satellite tmpSat = satellites.get(theOscMessage.addrPattern());
		String paramName = theOscMessage.get(0).stringValue();
		if(paramName.compareTo("id") == 0){
			tmpSat.setId(theOscMessage.get(1).intValue());
		} else if(paramName.compareTo("elev") == 0) {
			tmpSat.setElev(theOscMessage.get(1).intValue());
		} else if(paramName.compareTo("azim") == 0) {
			tmpSat.setAzim(theOscMessage.get(1).intValue());
		} else if(paramName.compareTo("noisy") == 0) {
			tmpSat.setNoise(theOscMessage.get(1).intValue());
		}
		
	}
	
	public void keyPressed() {
		if(key == 's' || key == 'S') {
			speakerMode = !speakerMode;
			if(speakerMode) {
				Collection<Satellite> sats = satellites.values();
				for (Satellite s: sats) {
					s.setElevSpeaker(275);
				}
			}
		}
	}

	public static void main(String _args[]) {
		PApplet.main(new String[] { satellitesoundp5.SatelliteSoundP5.class.getName() });
	}
}
