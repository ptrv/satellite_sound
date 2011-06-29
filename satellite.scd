
/*
	satellite.scd

	Peter Vasil
	TU Berlin, Klanganalyse und -synthese

*/


(
q = q ? ();
q.slotNames = (1..2).collect { |num| ("/SAT" ++ num).asSymbol };
q.satNames = (0..1).collect { |i| ("sat" ++ (1000 + i)).asSymbol };
q.satNames.do { |name| Ndef(name).ar(2); };

)