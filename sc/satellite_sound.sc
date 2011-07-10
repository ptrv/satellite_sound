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
		//synth = syntharg;
		oscnames = oscnamesarg ? [\id, \elev, \azim, \noisy];

		synth = Synth((synthnamearg ? \sputnik), [\dseq, List.fib(32)+2%5]).run(false);
		
		oscresponder = OSCresponderNode(nil, slotName, { |t, r, msg|
			msg.postln;

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
			
			(1..(satnumber+1)).collect { |num|
				slot = ("/SAT" ++ num).asSymbol;
			};
			(0..satnumber).collect { |j|
				name = ("sat" ++ (1000 + j)).asSymbol;
			};

			satellites.add(Satellite(slot, name));
		};
	}

	start {
		satellites.do { |satellite|
			satellite.synth.run(true);
		};

	}
	stop {
		satellites.do { |satellite|
			satellite.synth.run(false);
		};

	}
	add { arg slotname, name, synthname;
		satellites.add(Satellite(slotname, name, synthname));
	}

	removeResponders {
		satellites.do { |sat|
			sat.removeResponder;
		}
	}
}
