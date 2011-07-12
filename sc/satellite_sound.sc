Satellite {
	
	var slotName;
	var name;
	var <synth;
	var synthname;
	var oscnames;
	var <oscresponder;
	var lastcall;
	var alivecheckinterval=0.5;
	
	*new { arg slotname, name, synthname, oscnames, group; 
		^super.new.init(slotname, name, synthname, oscnames, group);
	}
	
	init { arg slotnamearg, namearg, synthnamearg, oscnamesarg, grouparg;
		slotName = slotnamearg;
		name = namearg;
		synthname = synthnamearg;
		oscnames = oscnamesarg;
		lastcall = 0;
		
		synth = Synth(synthname, target: grouparg).run(false);

		// OSC responder
		oscresponder = OSCresponderNode(nil, slotName, { |t, r, msg|
			lastcall = 0;
			oscnames.do{ arg oscname, i;
				synth.set(oscname, msg[i+1]);
			}
		}).add;

		// Check if satellite is alive
		SystemClock.sched(alivecheckinterval,{ arg time;
			if(lastcall > (alivecheckinterval*6), {
				// ("Stop" + name).postln;
				synth.run(false);
			},{
				// ("Start" + name).postln;
				synth.run(true);
			});
			lastcall = lastcall + alivecheckinterval;
		});
	}
}

SatelliteSound {
	var satellites;
	var satnumber;
	var <server;
	var srcGrp, efxGrp;
	*new { arg satnumber, server; 
		^super.new.init(satnumber, server);
	}
	
	*initClass {
		StartUp.add{
			SynthDef(\sputnik, { |amp=0.5, azim=30, elev=1,id=10,noisy=22,mix=0.4, room=0.7, damp = 0.2|
				var freq = elev.linexp(1,90,80,240);
				
				var vol_id = id.linlin(1,20,0.6,0.3);
				
				var xVal = noisy.linexp(0, 50, 1000, 10000);
				var yVal = elev.linexp(0, 40, 1000, 10000);
				
				var pos= azim.linlin(0,360,-1,1);
				var normElev = elev/90;
				var normAzim = azim/180 - 1;
				
				var level = elev.linlin(0,90,0.2, 1);
	
				var imp = noisy.linlin(0,40,0.5, 2);
				var trig = Impulse.kr(imp);
				var dseq = Control.names([\dseq]).kr(List.fib(32)+2%5);
				var seq = Dseq(dseq, inf);
				var trig2 = Demand.kr(trig, 0, seq * 0.4) * trig;
	
				var src = SinOsc.ar(
					Gendy3.ar(
						(id%6).rand,
						(id%6).rand,
						SinOsc.kr(0.1,0,0.49,0.51),
						SinOsc.kr(0.13,0,0.49,0.51),
						freq*2,
						SinOsc.kr(0.17,0,0.0049,0.0051),
						SinOsc.kr(0.19,0,0.0049,0.0051),
						12,
						12,
						200,
						400),0, 0.1);
				
				// EQ
				src = BPeakEQ.ar(src, 642, 0.31, 14);
				src = BHiShelf.ar(src, 1440, 1, 21);
	
				src = PanAz.ar(8,src, normAzim,level,2)*Decay.kr(trig2, 0.5)*vol_id*amp*3;
	
				Out.ar(0,src);
			}).store;
		}
	}
	init { arg satnumberarg, serverarg;

		satnumber = satnumberarg;
		server = serverarg ? Server.default;
		srcGrp= Group.head(server);
		efxGrp= Group.tail(server);
		
		satellites = Array.new(satnumber);

		satnumber.do { |i|
			var slot = ("/SAT" ++ (i+1)).asSymbol;
			var name = ("sat" ++ (1000 + i)).asSymbol;

			satellites.add(Satellite(slot, name, \sputnik, [\id, \elev, \azim, \noisy], srcGrp));
		};

		CmdPeriod.doOnce({
			satellites.do { |sat|
				sat.oscresponder.remove;
				sat.synth.free;
			};
		});
	}

	start {
		satellites.do { |sat|
			sat.synth.run(true);
		};
	}

	stop {
		satellites.do { |sat|
			sat.synth.run(false);
		};
	}
}
