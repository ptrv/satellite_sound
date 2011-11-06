

// Server
(
Server.default = s = Server.local;
s.options.numAudioBusChannels = 2048;
s.options.memSize = 2 ** 16;
s.latency = nil;
s.boot;

)


y = SatelliteSound(20, visualize: true);
y.start;
y.stop;

