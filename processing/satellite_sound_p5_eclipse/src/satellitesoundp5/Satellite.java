package satellitesoundp5;

import processing.core.PApplet;

public class Satellite {
	
	private String name;
	private int id;
	private PApplet pa;
	private int color;
	private Integrator elev;
	private Integrator azim;
	private Integrator noise;
	private Integrator elev2;
	
	public Satellite(PApplet p, String satName) {
		this.pa = p;
		this.name = satName;
		id = 0;
		color = pa.color(pa.random(255), pa.random(255), pa.random(255), 200);
		elev = new Integrator(0);
		azim = new Integrator(0);
		noise = new Integrator(0);
		elev2 = new Integrator(0);
		float attr = 0.2f;
		float damp = 0.3f;
		elev.damping = damp;
		elev.attraction = attr;
		elev2.damping = damp;
		elev2.attraction = attr	;
		azim.damping = damp;
		azim.attraction = attr;
		noise.damping = damp;
		noise.attraction = attr;
	}

	public void draw() {
		elev.update();
		azim.update();
		noise.update();
		elev2.update();
		if(noise.value > 0.5) {
			pa.fill(color);
			pa.noStroke();
			float elevation = 0;
			if(SatelliteSoundP5.speakerMode) {
				elevation = elev2.value;
				elev.value = elev2.value;
			} else {
				elevation = elev.value;
				elev2.value = elev.value;
			}
			float offsetX = elevation * PApplet.cos(azim.value);
			float offsetY = elevation * PApplet.sin(azim.value);
			pa.ellipse(pa.width/2 + offsetX, pa.height/2 + offsetY, noise.value, noise.value);
		}
	}

	public String getName() {
		return name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setElev(int elev) {
		this.elev.target(PApplet.map(elev, 0, 90, 50, 300));
	}
	public void setElevSpeaker(int elev) {
		this.elev2.target(elev);
	}

	public void setAzim(int azim) {
		this.azim.target(azim*PApplet.DEG_TO_RAD);
	}

	public void setNoise(int noise) {
		this.noise.target(PApplet.map(noise, 0, 50, 0, 50));
	}
}
