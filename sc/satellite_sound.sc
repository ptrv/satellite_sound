Satellite : Object {
	
	var slotName;
	var name;
	var <synth;
	var synthname;
	var oscnames;
	var oscresponder;
	
	*new { arg slotname, name, synthname, oscnames; 
		^super.new.init(slotname, name, synthname, oscnames);
	}
	
	init { arg slotnamearg, namearg, synthnamearg, oscnamesarg;
		slotName = slotnamearg;
		name = namearg;
		synthname = synthnamearg ? \sputnik;
		oscnames = oscnamesarg ? [\id, \elev, \azim, \noisy];

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

			var src = SinOsc.ar(Gendy3.ar((id%6).rand,(id%6).rand,SinOsc.kr(0.1,0,0.49,0.51),SinOsc.kr(0.13,0,0.49,0.51),freq*2, SinOsc.kr(0.17,0,0.0049,0.0051), SinOsc.kr(0.19,0,0.0049,0.0051), 12, 12, 200, 400), 0, 0.1);
						
			// EQ
			src = BPeakEQ.ar(src, 642, 0.31, 14);
			src = BHiShelf.ar(src, 1440, 1, 21);

			src = PanAz.ar(8,src, normAzim,level,2)*Decay.kr(trig2, 0.5)*vol_id*amp*3;

			Out.ar(0,src);
		}).load;

		synth = Synth(synthname);//.run(false);

		oscresponder = OSCresponderNode(nil, slotName, { |t, r, msg|
			oscnames.do{ arg oscname, i;
				synth.set(oscname, msg[i+1]);
			}
		}).add;
	}

	removeResponder {
		oscresponder.remove;
	}
}

SatelliteSound : Object {
	var satellites;
	var satnumber;
	*new { arg satnumber; 
		^super.new.init(satnumber);
	}
	
	init { arg satnumberarg;
		satnumber = satnumberarg;
		
		satellites = Array.new(satnumber);

		satnumber.do { |i|
			var slot;
			var name;
			
			slot = ("/SAT" ++ (i+1)).asSymbol;
			name = ("sat" ++ (1000 + i)).asSymbol;

			satellites.add(Satellite(slot, name));
		};

		CmdPeriod.doOnce({
			satnumber.do { |i|
				//satellites[i].synth.run(false);
				satellites[i].removeResponder;
				satellites[i].synth.free;
			};
		});
	}

	start {
		satnumber.do { |i|
			satellites[i].synth.run(true);
		};
	}

	stop {
		satnumber.do { |i|
			satellites[i].synth.run(false);
		};

	}
	// stopRemove {
	// 	satnumber.do { |i|
	// 		satellites[i].synth.run(false);
	// 		satellites[i].removeResponder;
	// 		satellites[i].synth.free;
	// 	};
	// }
	// add { arg slotname, name, synthname;
	// 	satellites.add(Satellite(slotname, name, synthname));
	// }

	// removeResponders {
	// 	satellites.do { |sat|
	// 		sat.removeResponder;
	// 	}
	// }
}
