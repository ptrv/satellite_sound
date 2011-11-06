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

	
	public Satellite(PApplet p, String satName) {
		this.pa = p;
		this.name = satName;
		id = 0;
		color = pa.color(pa.random(255), pa.random(255), pa.random(255));
		elev = new Integrator(0);
		azim = new Integrator(0);
		noise = new Integrator(0);
		float attr = 0.2f;
		float damp = 0.3f;
		elev.damping = damp;
		elev.attraction = attr;
		azim.damping = damp;
		azim.attraction = attr;
		noise.damping = damp;
		noise.attraction = attr;
	}

	public void draw() {
		elev.update();
		azim.update();
		noise.update();
		pa.fill(color);
		float offsetX = elev.value * PApplet.cos(azim.value);
		float offsetY = elev.value * PApplet.sin(azim.value);
		pa.ellipse(pa.width/2 + offsetX, pa.height/2 + offsetY, noise.value, noise.value);
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

	public void setAzim(int azim) {
		this.azim.target(azim*PApplet.DEG_TO_RAD);
	}

	public void setNoise(int noise) {
		this.noise.target(PApplet.map(noise, 0, 50, 5, 50));
	}
}
