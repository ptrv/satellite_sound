package satellitesoundp5;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;


public class SatelliteSoundP5 extends PApplet {

	OscP5 oscP5;
	
	HashMap<String, Satellite> satellites;
//	Satellite[] satellites;

	static final private int NUM_SATELLITES = 20;

	public void setup() {
		size(600, 600);
		oscP5 = new OscP5(this, 12000);
		
		satellites = new HashMap<String, Satellite>();
		for (int i = 0; i < NUM_SATELLITES ; i++) {
			String satKey = "/SAT"+(i+1);
			Satellite tmpSat = new Satellite(this);
			satellites.put(satKey, tmpSat);
		}
		
	}

	public void draw() {
		background(255);
		
		Collection<Satellite> sats = satellites.values();
		for (Satellite s: sats) {
			s.draw();
		}
	}

	public void oscEvent(OscMessage theOscMessage) {
		/* print the address pattern and the typetag of the received OscMessage */
//		print("### received an osc message.");
//		print(" addrpattern: "+theOscMessage.addrPattern());
//		println(" typetag: "+theOscMessage.typetag());
		
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
	public static void main(String _args[]) {
		PApplet.main(new String[] { satellitesoundp5.SatelliteSoundP5.class.getName() });
	}
}
