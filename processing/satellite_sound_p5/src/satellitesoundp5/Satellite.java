package satellitesoundp5;

import processing.core.PApplet;

public class Satellite {
	
	
	private String name;
	private int id;
	private int elev;
	private float azim;
	private float noise;
	private PApplet pa;
	
	public Satellite(PApplet p) {
		this.pa = p;
		id = 0;
		elev = 0;
		azim = 0;
		noise = 0;
	}

	public void draw() {
		pa.fill(255, 0, 0);
		float offsetX = (pa.width/2-20) * PApplet.sin(noise) * PApplet.sin(azim);
		float offsetY = (pa.height/2-20) * PApplet.sin(noise) * PApplet.cos(azim);
		pa.ellipse(pa.width/2 + offsetX, pa.height/2 + offsetY, 10, 10);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getElev() {
		return elev;
	}

	public void setElev(int elev) {
		this.elev = elev;
	}

	public void setAzim(int azim) {
		this.azim = PApplet.map(azim, 0,360, 0, 1);
	}

	public void setNoise(int noise) {
		this.noise = PApplet.map(noise, 0, 50, 0, 1);
	}
}
