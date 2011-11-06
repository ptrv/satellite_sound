/*
	satellites_alt.scd

	Peter Vasil
*/

//http://new-supercollider-mailing-lists-forums-use-these.2681727.n2.nabble.com/Level-meter-disappearing-td2714612.html
(
SynthDef(\bell, { |amp = 0.1, out = 0, azim=30, elev=1,id=10,noisy=22|
	var freq = id.linexp(1,30,200,800);
	
	var vol_id = id.linlin(1,20,0.6,0.3);
	
	// var xVal = noisy.linexp(0, 50, 1000, 10000);
	// var yVal = elev.linexp(0, 40, 1000, 10000);
	
	var pos= azim.linlin(0,360,-1,1);
	var normElev = elev/90;
	var normAzim = azim/180 - 1;
	
	var level = elev.linlin(0,90,0.2, 1);
	
	var imp = noisy.linlin(0,40,0.5, 2);
	
	var exciter = PinkNoise.ar(amp*noisy.linlin(0,40,0.5, 2)) * Decay2.kr(Impulse.kr(0), 0.01, 0.1);
	var sig = Klank.ar(`[
		[freq, freq*1.6, freq*2.1] ,
		0.1,
		{ elev.linlin(0,90,1.0, 3.0) } ! 1,
	], exciter);
	var pan;
	DetectSilence.ar(sig, doneAction: 2);

	sig = PanAz.ar(2, sig, normAzim, level, 2);

	Out.ar(0, sig);
}).add;
)

(
q = ();

q.slotNames = (1..20).collect { |num| ("/SAT" ++ num).asSymbol };
q.satNames = (0..19).collect { |i| ("sat" ++ (1000 + i)).asSymbol };


q.responders = 19.collect {|i|
	OSCresponderNode(nil, q.slotNames[i], { |t, r, msg|
		Synth(\bell, [\id, msg[1], \elev, msg[2], \azim, msg[3], \noisy, msg[4]]);
	}).add;
};

CmdPeriod.doOnce({
	q.responders.do {|responder|
		responder.remove;
	};
});

);
