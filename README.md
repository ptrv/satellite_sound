# Satellite Sound

### install

copy or symlink `sc/` to Extensions directory.  
Mac: `~/Library/Application\ Support/SuperCollider/Extensions`  
Linux: `~/share/SuperCollider/Extensions`

### usage

Modify `PORT` in python/SatelliteLogReader.py to your needs.  
Default port is 57120.  
Hint: You can look up the port for SuperCollider by evaluating
following line in SuperCollider:

    NetAddr.langPort;

Start python script `SatelliteLogReader.py` by running (current working directory is `satellite_sound/`):

    python python/SatelliteLogReader.py <logfile> <interval>

Start Server and evaluate the following block in SuperCollider:

     (
     SatelliteSound(20);
     )
