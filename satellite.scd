
/*
	satellite.scd

	Peter Vasil
	TU Berlin, Klanganalyse und -synthese

*/

// Server
(
Server.default = s = Server.internal;
s.options.numAudioBusChannels = 2048;

s.latency = nil;
s.boot;

)
// All
(

// Events

(
q = ();

q.slotNames = (1..20).collect { |num| ("/SAT" ++ num).asSymbol };
q.satNames = (0..19).collect { |i| ("sat" ++ (1000 + i)).asSymbol };
q.satNames.do { |name| Ndef(name).ar(8); };
q.satNames.do { |name| Pdef(name); };

q.satValues = ();
q.satNames.do {|name| q.satValues[name.asSymbol]= [0,0,0,0];};
q.satValues.postcs;

q.seqs = ();
q.satNames.do {|name| q.seqs[name.asSymbol] = [4,0,0,1,2,1,0,1]; };

q.synths = ();
q.satNames.do {|name|
	SynthDef(name, { |amp=0.05, azim=30, elev=1,id=10,noisy=22,dseq=#[4,0,1,0,0,1,0]|
		var freq = elev.linexp(1,90,80,240);
	
		var vol_id = id.explin(1,20,0.8,0.4);
	
		var xVal = noisy.linexp(0, 50, 1000, 10000);
		var yVal = elev.linexp(0, 40, 1000, 10000);
		
		var pos= azim.linlin(0,360,-1,1);
		var normElev = elev/90;
		var normAzim = azim/180 - 1;
	
		var level = elev.linlin(0,90,0.2, 1);

		var imp = noisy.linexp(0,40,1, 12);
		var trig = Impulse.kr(imp);
		var seq = Dseq(dseq, inf);
		var trig2 = Demand.kr(trig, 0, seq * 0.4) * trig;
	
		
//		var src = Normalizer.ar(
//					RLPF.ar(
//					RLPF.ar(Gendy3.ar(ampdist: 2,durdist: 4, freq: freq),
//					xVal,0.05),
//					yVal,0.05),
//					0.9);
	
		var src = SinOsc.ar(Gendy3.ar((id%6).rand,(id%6).rand,SinOsc.kr(0.1,0,0.49,0.51),SinOsc.kr(0.13,0,0.49,0.51),freq*2, SinOsc.kr(0.17,0,0.0049,0.0051), SinOsc.kr(0.19,0,0.0049,0.0051), 12, 12, 200, 400), 0, 0.1);
//		Out.ar(0,PanAz.ar(8,src, normAzim,level,2)*0.5;);
		Out.ar(0,PanAz.ar(8,src, normAzim,level,2)*Decay.kr(trig2, 0.5)*vol_id*3;);
	}).send(s);
//	q.synths[name.asSymbol] = Synth(name, [\dseq, List.fib(32)%(List.fib(64)%12)]);
//	q.synths[name.asSymbol] = Synth(name, [\dseq, List.fib(32)%9/3]);
	q.synths[name.asSymbol] = Synth(name, [\dseq, List.fib(32)+2%5]);
	q.synths[name.asSymbol].play;
};
 
q.satNames.do { |name|
};
);


// OSC listener
(
thisProcess.recvOSCfunc = { |t, r, msg| 
	var name = msg[0];
	var satname;
	 
	
	if (q.slotNames.includes(name)) { 
		satname = q.satNames[msg[1]]; // names start with 1, indices with 0
//		satname.postln;
		if (satname.notNil) { // e.g. if satID is too high!
			
			//msg.postcs;
			
			q.synths.at(satname).set(
				\id, msg[1], 
				\elev, msg[2], 
				\azim, msg[3], 
				\noisy, msg[4]
			);			
			q.satValues.put(satname, [msg[1],msg[2],msg[3],msg[4]]);
			};
	};
};
);


);



(	
o = OSCresponder(nil, '/RMC', { |t, r, m| 
	m.postln; // debug
 	m[0].postln;
 	m[1].postln;
 	m[2].postln;
 	m[3].postln;
 	m[4].postln;
 	m[5].postln;
 	
	//Ndef(\LocalInfo).set(\lat,m[2],\lon,m[3]).play;
	//"RMC on".postln;
		
}).add;
);


o.remove;


(

q.satNames.do { |satname|
	Ndef(satname).source =  {|amp=0.05, azim=30, elev=1,id=10,noisy=22|
	
	// freq
	var freq = elev.linexp(1,90,120,120*4);

	var xVal = noisy.linexp(0, 50, 1000, 6000);
	var yVal = elev.linexp(0, 40, 1000, 6000);
	
	var pos= azim.linlin(0,360,-1,1);
	var normElev = elev/90;
	var normAzim = azim/180 - 1;

	var level = elev.linlin(0,90,0.2, 1);
	
//	var trig = Impulse.kr(8);
//	var seq = Dseq(dseq, inf);
//	var trig2 = Demand.kr(trig, 0, seq * 0.4) * trig;
	
	var src = Normalizer.ar(
				RLPF.ar(Gendy3.ar(freq: freq),
				xVal,0.05),
				0.9);

//	PanAz.ar(8,src, normAzim,level,2)*Decay.kr(trig2, 0.5);
	PanAz.ar(8,src, normAzim,level,2)*0.5;
};    
};

// ALIVE OR NOT
Tdef(\alive, { 
	inf.do { 
		//q.satStrengths;
		//"test".postln;
		5.wait; // refreshing time according to data rate.
	};
}).play;

//Pdef
);

