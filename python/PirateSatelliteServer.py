#!/usr/bin/env python
#
# Daniel Belasco Rogers 2011 (danbelasco@yahoo.co.uk)
#
# The intention of this script is to send the output of a connected
# GPS (I usually use a Qstarz Q1000x) to a listening programme
# (puredata, supercollider, processing) in neatly packaged bundles
#
# Options:
# -f --file Write the osc bundles to a pickle file for replaying
# later with my script 'SatelliteLogReader.py'
# -p --prn order by prn (unique satellite number) instead of
# azimuth (default)
# -d --dumb Do not send gps values over osc (for
# debugging if you haven't set up a receiving script yet')
#
# Example output (carriage returns are mine):
# /RMC ['12:58:27', 52.496204376220703, 13.445575714111328, 0.0, 0.0],
# /SAT1 [6, 6, 27, 0], /SAT2 [3, 8, 40,0],
# /SAT3 [7, 58, 65, 0], /SAT4 [19, 2, 67, 0],
# /SAT5 [13, 18, 98, 0], /SAT6 [28, 25, 159, 0],
# /SAT7 [8, 82, 168, 0], /SAT8 [10, 43,174, 0],
# /SAT9 [2, 7, 227, 0], /SAT10 [5, 62, 263, 0],
# /SAT11 [26,34, 284, 0], /SAT12 [21, 5, 339, 0]
#
# Last Update: Thu 21 Apr 2011 03:08:16 PM CEST
#
# A re-write of splitbundleGSV-RMC-newOSC.py
# for Andre Wakko and my project 'Pirate Satellite Synthesizer'
#
# Requires pyOSC and gpsdevice (should be bundled with this script)

try:
    import OSC
except ImportError:
    print """Please install pyOSC from the following location:
https://trac.v2.nl/wiki/pyOSC"""

import cPickle
import sys
import os.path
from time import sleep
from optparse import OptionParser
try:
    from gpsdevice import *
except ImportError:
    print """
This script requires the script 'gpsdevice.py' to be in the same
folder or in the PYTHONPATH
"""

# Port and address for sending osc messages
# Address is localhost (this machine by default)
# Remember to set port number in receiving programme
OSCADDR = "127.0.0.1"
OSCPORT = 57120
# Usual location (address) of gps plugged into usb (linux)
GPSADDR = "/dev/ttyUSB0"


def reorderList(List, prn):
    '''
    takes an unsorted list of satellite values and returns a
    sorted list by azimuth or prn

    List = list of satellite values
    prn = Boolean, from options if true, order by prn not azimuth
    Dict = dictionary created for sorting purposes only
    '''
    Dict = {}
    for i in range(0, len(List), 4):
        if prn:
            Dict[List[i]] = (List[i],
                             List[i + 1],
                             List[i + 2],
                             List[i + 3])
        if not prn:
            Dict[List[i + 2]] = (List[i],
                             List[i + 1],
                             List[i + 2],
                             List[i + 3])
    keys = Dict.keys()
    keys.sort()
    sortedList = [Dict[key] for key in keys]
    List = []
    for item in sortedList:
        for element in item:
            List.append(element)
    return List


def askyesno(question):
    """
    handles yes/no questions
    question = what to ask the user at the prompt
    answer = a boolean True for yes and False for no
    """
    while 1:
        answer = raw_input(question).lower()
        if answer in ('y', 'yes', 'n', 'no'):
            if answer in ('y', 'yes'):
                answer = True
            if answer in ('n', 'no'):
                answer = False
            return answer
        print '\nplease answer y or n\n'


def main():
    """
    the main loop - consider splitting these functions up for greater
    clarity
    """
    ############################################################
    # script options
    ############################################################
    usage = "usage: %prog '-f=filename' '-d'"
    parser = OptionParser(usage, version="1.0")
    parser.add_option("-f", "--file", dest="filename",
                      help="write binary OSC messages to pickle FILE",
                      metavar="FILE")
    parser.add_option("-d", "--dumb", dest="dumb",
                      help="don't send messages to OSC", action="store_true",
                      default=False)
    parser.add_option("-p", "--prn", dest="prn",
                      help="sort by prn (default by azi)", action="store_true",
                      default=False)
    (options, args) = parser.parse_args()
    if options.filename:
        if (os.path.isfile(options.filename)):
            answer = askyesno('file already exists - overwrite? y/n ')
            if not answer:
                return 0
        log = open(options.filename, 'wb')
    ############################################################
    # set up osc and gps connections
    ############################################################
    # Osc message address and port
    send_address = (OSCADDR, OSCPORT)
    # OSC basic client
    osc = OSC.OSCClient()
    # set the address for all following messages
    osc.connect(send_address)
    # setup and connect to gps
    gps = GPSDevice(GPSADDR)
    gps.open()
    ############################################################
    # start reading the values and sending them
    ############################################################
    # initialise local vars
    GSVcollection = []
    FirstRMC = True
    # initiate the bundle
    bundle = OSC.OSCBundle()
    # main read / send loop
    try:
        for record in gps.read_all():
            if record['sentence'] == 'RMC':
                # on receiving an RMC sentence, gather everything
                # together and send it.
                if FirstRMC == False:
                    # Skip reorderList if all the azi values are zero
                    if (GSVcollection[2] != 0 and
                        GSVcollection[6] != 0 and
                        GSVcollection[10] != 0 and
                        GSVcollection[14] != 0):
                        # sort complete GSV sentence by azimuth
                        GSVcollection = reorderList(GSVcollection, options.prn)
                    #bundle.setAddress("/SAT")
                    count = 0
                    for i in range(0, len(GSVcollection), 4):
                        count += 1
                        print count, GSVcollection[i:i + 4]
                        #bundle.setAddress("/SAT%d" % count)
                        bundle.setAddress("/SAT%d" % GSVcollection[i])
                        bundle.append(GSVcollection[i:i + 4])
                    GSVcollection = []
                    # Send over OSC if the dumb option is off
                    if options.dumb == False:
                        osc.send(bundle)
                    print bundle
                    if options.filename:
                        cPickle.dump(bundle, log)
                    #  clear the bundle otherwise it just fills up
                    bundle.clearData()
                # the below only happens once - the first time an RMC
                # sentence is received
                elif FirstRMC == True:
                    FirstRMC = False
                RMClist = [
                record['time'],
                record['latitude'],
                record['longitude'],
                record['knots'],
                record['bearing'],
                ]
                # append to bundle with RMC prefix
                bundle.append({'addr': "/RMC", 'args': RMClist})
            if record['sentence'] == 'GSV':
                if FirstRMC == False:
                    for item in record['satellite_data_list']:
                        GSVcollection.append(item)
    except KeyboardInterrupt:
        osc.close()
        gps.close()
        if options.filename:
            log.close()
        print """
user interupt, shutting down
"""
        sys.exit


if __name__ == '__main__':
    sys.exit(main())
