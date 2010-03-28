#!/usr/bin/perl -w

# A quick and dirty script for parsing tcpdump files
# to produce csv files containing counts on specific
# packets like tcp, udp, icmp, and web.

# NOTE: Large dump file sizes can result in out of
# memory errors due to perl being unable to free
# memory and dump print buffers.

use strict;
use Switch;

my $HOST = "192.168.0.9";

my %pckt_hash = ();
my $num_dumps = `ls -l dump[^\.]* | wc -l`;
chomp($num_dumps);
print "$num_dumps\n";

open(TCPFILE,  ">>tcp-traffic-secs.csv");
open(UDPFILE,  ">>udp-traffic-secs.csv");
open(ICMPFILE, ">>icmp-traffic-secs.csv");
open(WEBFILE,  ">>web-traffic-secs.csv");
open(OTHFILE,  ">>oth-traffic-secs.csv");

my $curr_time = -1;
my $prev_time = -1;

$|=1; # flush print buffer every print

for(my $i = 0; $i < $num_dumps; ++$i) {
  my $dump = `tcpdump -nvr dump$i`;
  $dump =~ s/^SMB[^\n]*\n//mg;
  $dump =~ s/\n\s\s*//mg;
  $dump =~ s/\n\n*/\n/mg;
  my @lines = split(/\n/, $dump);
  undef $dump;
  for my $line (@lines) {
    my ($time, $prot, $junk, $port, $src_ip, $src_port, $dst_ip, $dst_port);
  
    next if($line =~ m/\s*STP\s*/);
    next if($line =~ m/\s*ARP\s*/);
  
    # PARSE
    ($time, $line) = split(/\s/, $line, 2);
    ($junk, $line) = split(/proto\s*/, $line, 2);
    ($prot, $line) = split(/\s/, $line, 2);
    ($junk, $line) = split(/length\s*[0-9]+\)/, $line, 2);
  
    $line =~ s/(([0-9]+\.){4})([0-9]+)/$1_$3/g;
  
    if($prot eq 'ICMP') {
      ($src_ip, $line) = split(/\s*>\s*/, $line, 2);
      ($dst_ip, $line) = split(/:/, $line, 2);
      $src_port = '';
      $dst_port = '';
    }
    else {
      ($src_ip,   $line) = split(/\._/, $line, 2);
      ($src_port, $line) = split(/\s*>\s*/, $line, 2);
      ($dst_ip,   $line) = split(/\._/, $line, 2);
      ($dst_port, $line) = split(/:/, $line, 2);
    }
  
    if($src_port ne "80" && $dst_port ne "80") { # Output uncommon traffic
      print "$time $prot $src_ip:$src_port $dst_ip:$dst_port\n";
    }
  
    # COUNT
    next unless($dst_ip eq $HOST); # only inbound traffic
  
    $pckt_hash{ $prot } = 0 unless(exists $pckt_hash{ $prot });
    $pckt_hash{ $prot } = $pckt_hash{ $prot } + 1;
  
    if($prot ne 'ICMP') {
      $pckt_hash{ $dst_port } = 0 unless(exists $pckt_hash{ $dst_port });
      $pckt_hash{ $dst_port } = $pckt_hash{ $dst_port } + 1;
    }
  
    # WRITE
    ($curr_time = $time) =~ s/\..*//; # remove fractional seconds from time
    $prev_time = $curr_time if($prev_time eq "-1"); # initial case
    if($prev_time ne $curr_time) {
      while( my ($key, $value) = each %pckt_hash ) {
        switch($key) {
          case 'TCP'  { print TCPFILE  "$prev_time,$key,$value\n"; }
          case 'UDP'  { print UDPFILE  "$prev_time,$key,$value\n"; } 
          case 'ICMP' { print ICMPFILE "$prev_time,$key,$value\n"; }
          case '80'   { print WEBFILE  "$prev_time,$key,$value\n"; }
          else        { print OTHFILE  "$prev_time,$key,$value\n"; }
        }
      }
      %pckt_hash = ();
    }
    $prev_time = $curr_time;
  }
}

close(TCPFILE);
close(UDPFILE);
close(ICMPFILE);
close(WEBFILE);
close(OTHFILE);

exit 0;