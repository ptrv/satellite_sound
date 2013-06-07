import java.util.Collection;
import java.util.HashMap;

import oscP5.OscMessage;
import oscP5.OscP5;

OscP5 oscP5;

HashMap<String, Satellite> satellites;

PFont font;

boolean speakerMode = false;


static final private int NUM_SATELLITES = 20;

void setup() {
		size(600, 600);
		oscP5 = new OscP5(this, 12000);

		font = loadFont("Ubuntu-48.vlw");
		textFont(font, 12);

		satellites = new HashMap<String, Satellite>();
		for (int i = 0; i < NUM_SATELLITES ; i++) {
			String satKey = "/SAT"+(i+1);
			Satellite tmpSat = new Satellite(satKey);
			satellites.put(satKey, tmpSat);
		}
	}

void draw() {
		background(240);
		pushMatrix();
		rotate(-HALF_PI);
		translate(-width, 0);
		if(speakerMode) {
			drawSpeakerCircle();
		} else {
			drawCircles();
		}

		Collection<Satellite> sats = satellites.values();
		for (Satellite s: sats) {
			s.draw();
		}
		popMatrix();
		fill(0);
		text("satellite sound", 10, 15);
		text("ptrv, 2013", width-60, height-10);
		if(!speakerMode) {
			// zero degree
			text("0\u00B0", width/2, 15);
		}
	}

void drawSpeakerCircle() {
		stroke(100);
		strokeWeight(2);
		noFill();
		ellipse(width/2, height/2, 500, 500);
	}

void drawCircles() {
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

void oscEvent(OscMessage theOscMessage) {
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

void keyPressed() {
  if(key == 's' || key == 'S') {
    speakerMode = !speakerMode;
    if(speakerMode) {
      Collection<Satellite> sats = satellites.values();
      for (Satellite s: sats) {
        s.setElevSpeaker(250);
      }
    }
  }
}
