/*
	satellite_sound.sc

	Peter Vasil
*/

Satellite {

	var slotName;
	var name;
	var <synth;
	var synthname;
	var oscnames;
	var <oscresponder;
	var timestamp;
	var <>active;
	var <>pause = false;
	var remoteVisu;
	
	*new { arg slotname, name, synthname, oscnames,
		group, updateinterval, visualize, remoteVisu; 
		^super.new.init(slotname, name, synthname, oscnames,
			group, updateinterval, visualize, remoteVisu);
	}
	
	init { arg slotnamearg, namearg, synthnamearg, oscnamesarg,
		grouparg, updateintervalarg, visualizearg, remoteVisuArg;

		slotName = slotnamearg;
		name = namearg;
		synthname = synthnamearg;
		oscnames = oscnamesarg;
		timestamp = Date.getDate.rawSeconds - 2;
		synth = Synth(synthname, target: grouparg).run(false);
		active = false;
		//visualize = visualizearg ? true;
		remoteVisu = remoteVisuArg;

		// OSC responder setting Satellite parameter and
		// sending to remote visualization application
		oscresponder = OSCresponderNode(nil, slotName, { |t, r, msg|
			timestamp = Date.getDate.rawSeconds;
			// timestamp.postln;
			oscnames.do{ arg oscname, i;
				synth.set(oscname, msg[i+1]);
				if(visualizearg, {
					remoteVisu.sendMsg(slotName,oscname,msg[i+1]);
				});
				
			}
		}).add;

		// Check if satellite is alive
		SystemClock.sched(0.5,{ arg time;
			var deltaT = Date.getDate.rawSeconds - timestamp;
			//deltaT.postln;

			if(deltaT > (updateintervalarg+1), {
				// ("Stop" + name).postln;
				synth.run(false);
				active = false;
			},{
				// ("Start" + name).postln;
				if(pause == false, {
					synth.run(true);
					active = true;
				})
			});
			0.5;
		});
	}
}

SatelliteSound {
	var satellites;
	var satnumber;
	var <server;
	var srcGrp, efxGrp;
	var activeSatellites;
	var synthname;
	var walkResponder;
	var lastLon=0, lastLat=0;
	var remoteAddr;
	var visualize = true;

	*new { arg satnumber, synthname, server, updateinterval,
		visualize, remoteport=12000, remoteserver="127.0.0.1"; 
		^super.new.init(satnumber, synthname, server,
			updateinterval, visualize, remoteport, remoteserver);
	}
	
	*initClass {
		StartUp.add{
			SynthDef(\sputnik, { |amp=0.5, azim=30, elev=1,id=10,noisy=22|
				var freq = elev.linexp(1,90,80,240);
				
				var vol_id = id.linlin(1,20,0.6,0.3);
				
				var xVal = noisy.linexp(0, 50, 1000, 10000);
				var yVal = elev.linexp(0, 40, 1000, 10000);
				
				var pos= azim.linlin(0,360,-1,1);
				var normElev = elev/90;
				var normAzim = azim/180 - 1;
				
				var level = elev.linlin(0,90,0.2, 1);
	
				var imp = noisy.linlin(0,40,0.5, 4);
				var trig = Impulse.kr(imp);
				//var dseq = Control.names([\dseq]).kr(List.fib(32)+2%5);
				var dseq = Control.names([\dseq]).kr([0,0,0,0,0,0,0,0]);
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
	
				src = PanAz.ar(8, src, normAzim,level,3)*Decay.kr(trig2, 0.5)*vol_id*amp*5;
	
				Out.ar(0,src);
			}).store;
		}
	}
	init { arg satnumberarg, synthnamearg, serverarg, updateintervalarg,
		visualizearg, remoteportarg, remoteserverarg;

		satnumber = satnumberarg;
		server = serverarg ? Server.default;
		srcGrp= Group.head(server);
		efxGrp= Group.tail(server);
		
		remoteAddr = NetAddr(remoteserverarg, remoteportarg);
		
		satellites = Array.new(satnumber);
		activeSatellites = Array.new(satnumber);

		synthname = synthnamearg ? \sputnik;

		if(server.serverRunning.not, {
			(this.class.asString++": server not running").error;
			this.halt;
		});
		
		visualize = visualizearg ? true;
		
		satnumber.do { |i|
			var slot = ("/SAT" ++ (i+1)).asSymbol;
			var name = ("sat" ++ (1000 + i)).asSymbol;

			satellites.add(
				Satellite(
					slot, name, synthname, 
					[\id, \elev, \azim, \noisy], 
					srcGrp, updateintervalarg ? 1,
					visualize, remoteAddr
				)
			);
			activeSatellites.add(false);
		};

		CmdPeriod.doOnce({
			satellites.do { |sat|
				sat.oscresponder.remove;
				sat.synth.free;
			};
			walkResponder.remove;
		});
		walkResponder = OSCresponder(nil, '/RMC', { |t, r, m| 
			
			var numberSequence;
//			m.postln; // debug
//		 	m[0].postln;
//		 	m[1].postln;
//		 	m[2].postln;
//		 	m[3].postln;
//		 	m[4].postln;
//		 	m[5].postln;
		 	
		 	numberSequence = this.calculateNumberSequence(m[2], m[3], m[4]);
			//numberSequence.postln;
			this.changePatterns(\dseq,numberSequence);
			
		}).add;

	}

	start {
		satellites.do { |sat, i|
			i.postln;
			if(activeSatellites[i] == true, {
				sat.synth.run(true);
				sat.active = true;
			});
			sat.pause = false;
		};
	}

	stop {
		satellites.do { |sat|
			this.numActiveSatellites;
			sat.synth.run(false);
			sat.active = false;
			sat.pause = true;
		};
	}
	
	changePatterns {|paramName, array|
		satellites.do { |sat|
			//"change pattern".postln;
			sat.synth.setn(paramName, array);
		};	
	}
	
	calculateNumberSequence { |num1, num2, num3|
		var numberSequence1, numberSequence2, numBits=32;
		var numberSequence, factors;
		
		numberSequence1 = num1.as32Bits.asBinaryDigits(32);
		numberSequence2 = num2.as32Bits.asBinaryDigits(32);
		numberSequence = Array.newClear(32);

		factors = num3.as32Bits.asDigits;
		
		numBits.do {|i|
			if( numberSequence1[i] == numberSequence2[i], {
				numberSequence.put(i, factors.reverse.at(i%factors.size));
			},{
				numberSequence.put(i, 0);
			});
		};
		^numberSequence.reverse;

	}

	numActiveSatellites {
		var numActiv = 0;
		satellites.do { |sat, i|
			if(sat.active, {
				numActiv = numActiv + 1;
				activeSatellites.put(i, true);
			},{
				activeSatellites.put(i, false);
			});
		};
		^numActiv;
	}
}
