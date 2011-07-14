# Satellite Sound

### install

copy `sc/` to Extensions directory.  
Mac: `~/Library/Application\ Support/SuperCollider/Extensions`  
Linux: `~/share/SuperCollider/Extensions`

### usage

Modify `PORT` in python/SatelliteLogReader.py to your needs.  
Hint: You can look up the port for SuperCollider by evaluating
following line in SuperCollider:

      NetAddr.langPort;

Start python script `SatelliteLogReader.py` by running:

      python python/SatelliteLogReader.py <logfile> <interval>

Evaluate the following block in SuperCollider:

     (
     SatelliteSound(20);
     )
