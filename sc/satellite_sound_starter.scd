

// Server
(
Server.default = s = Server.local;
s.options.numAudioBusChannels = 2048;

s.latency = nil;
s.boot;

)

// SynthDef

(
SynthDef(\sputnik, { |amp=0.5, azim=30, elev=1,id=10,noisy=22,mix=0.4, room=0.7, damp = 0.2|
		var freq = elev.linexp(1,90,80,240);
	
		var vol_id = id.linlin(1,20,0.6,0.3);
	
		var xVal = noisy.linexp(0, 50, 1000, 10000);
		var yVal = elev.linexp(0, 40, 1000, 10000);
		
		var pos= azim.linlin(0,360,-1,1);
		var normElev = elev/90;
		var normAzim = azim/180 - 1;
	
		var level = elev.linlin(0,90,0.2, 1);

		var imp = noisy.linlin(0,40,05, 2);
		var trig = Impulse.kr(imp);
		var dseq = Control.names([\dseq]).kr(List.fib(32)+2%5);
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
		
		// EQ
		src = BPeakEQ.ar(src, 642, 0.31, 14);
		src = BHiShelf.ar(src, 1440, 1, 21);

		src = PanAz.ar(8,src, normAzim,level,2)*Decay.kr(trig2, 0.5)*vol_id*amp*3;
//		src = FreeVerb.ar(
//			src, // mono src
//			mix, // mix 0-1
//			room, // room 0-1
//			damp // damp 0-1 duh
//		);
		
		Out.ar(0,src;);
}).add;

)

(
var satSound = SatelliteSound(20);
satSound.start;
