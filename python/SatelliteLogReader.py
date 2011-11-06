#!/usr/bin/env python
"""
SatelliteLogReader.py

Daniel Belasco Rogers 2011 (danbelasco@yahoo.co.uk)

takes a pickled file containing OSC bundles, made by
PirateSatelliteServer.py and sends the bundles over OSC to a
receiving programme, listening on port PORT (currently using 57120)
at a regular interval of seconds

Takes two arguments: location of log file and sending interval as a
positive integer

Example: python SatelliteLogReader.py data/test4.log 1 -n

This reads from file test4.log and sends the bundles therein every
(1) second. The '-n' option is 'no loop' which only sends the file
contents once and then exits. If this option is not enabled, the
default behaviour is to loop the file indefinately.
"""
# TODO: sort out looping (re-sending the bundles from the beginning of
# the file again)

import pickle
try:
    import OSC
except ImportError:
    print """
Please install pyOSC from the following location:
https://trac.v2.nl/wiki/pyOSC
"""
import sys
from time import sleep
from optparse import OptionParser

# port number to send osc messages to
# set this in receiving programme too
PORT = 57120


def open_pickle(logfile):
    """
    Tries to open the pickle file passed through logfile and handles
    errors if necessary
    """
    try:
        picklefile = open(logfile, 'rb')
    except IOError as err:
        print """
%s
There was a problem with the file you specified.
Did you type the name and location correctly?
""" % err
        sys.exit()
    return picklefile


def send_bundles(osc, logfile, secs):
    """
    Attempts to send osc bundles from pickle file and handle errors
    """
    picklefile = open_pickle(logfile)
    while 1:
        try:
            message = pickle.load(picklefile)
            try:
                osc.send(message)
            except OSC.OSCClientError as err:
                print """
%s
Have you started the receiving programme?
""" % err
                return
            sleep(secs)
            print message, type(message)
            print
        except EOFError:
            return
        except KeyError:
            print """
There was a problem with the file you selected.
It seems to be of the wrong type for this script.
Please select another file.
"""
            sys.exit()


def main():
    """
    Main function.
    """
    usage = "usage: %prog filename seconds"
    parser = OptionParser(usage, version="%prog 0.1")
    parser.add_option("-n", "--noloop", dest="noloop",
                      help="don't loop the log file", action="store_true",
                      default=False)
    (options, args) = parser.parse_args()
    if len(args) != 2:
        parser.error("specify a file path and sending interval in seconds")
    logfile = args[0]
    try:
        secs = float(args[1])
    except ValueError as err:
        print """
%s
Please enter a positive integer for the seconds
""" % err
        return
    # localhost and port settings - remember to set this in the
    # receiving programmpe too
    send_address = ("127.0.0.1", PORT)
    # OSC basic client
    osc = OSC.OSCClient()
    osc.connect(send_address) # set the address for all following messages
    if options.noloop == False:
        while 1:
            send_bundles(osc, logfile, secs)
    else:
        send_bundles(osc, logfile, secs)
        print """'no loop' selected, script ends here
"""
        return


if __name__ == '__main__':
    sys.exit(main())
