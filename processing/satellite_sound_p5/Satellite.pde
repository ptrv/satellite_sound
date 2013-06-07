class Satellite {

	String name;
	int id;
	PApplet pa;
	color satColor;
	Integrator elev;
	Integrator azim;
	Integrator noise;
	Integrator elev2;

	public Satellite(String satName) {
		this.name = satName;
		id = 0;
		satColor = color(random(255), random(255), random(255), 200);
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

	void draw() {
		elev.update();
		azim.update();
		noise.update();
		elev2.update();
		if(noise.value > 0.5) {
			fill(satColor);
			noStroke();
			float elevation = 0;
			if(speakerMode) {
				elevation = elev2.value;
				elev.value = elev2.value;
			} else {
				elevation = elev.value;
				elev2.value = elev.value;
			}
			float offsetX = elevation * cos(azim.value);
			float offsetY = elevation * sin(azim.value);
			ellipse(width/2 + offsetX, height/2 + offsetY, noise.value, noise.value);
		}
	}

	String getName() {
		return name;
	}

	void setId(int id) {
		this.id = id;
	}

	void setElev(int elev) {
		this.elev.target(map(elev, 0, 90, 50, 300));
	}
	void setElevSpeaker(int elev) {
		this.elev2.target(elev);
	}

	void setAzim(int azim) {
		this.azim.target(azim * DEG_TO_RAD);
	}

	void setNoise(int noise) {
		this.noise.target(map(noise, 0, 50, 0, 50));
	}
}
