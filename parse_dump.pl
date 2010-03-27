#!/usr/bin/perl -w
use strict;
use Switch;

my $HOST = "192.168.0.9";

my %prot_hash = ();
my $num_dumps = `ls -l dump.txt* | wc -l`;
chomp($num_dumps);
print "$num_dumps\n";


open(TCPFILE,  ">tcp-traffic.txt");
open(UDPFILE,  ">udp-traffic.txt");
open(ICMPFILE, ">icmp-traffic.txt");
open(WEBFILE,  ">web-traffic.txt");
open(OTHFILE,  ">oth-traffic.txt");

my $curr_time = -1;
my $prev_time = -1;

for(my $i = 0; $i < $num_dumps; ++$i) {
  my $dump = `tcpdump -nvr dump.txt$i`;
  $dump =~ s/\n\s\s*//g;
  my @lines = split(/\n/, $dump);
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

    if($src_port ne "80" && $dst_port ne "80") {
      print "$time $prot $src_ip:$src_port $dst_ip:$dst_port\n";
    }

    # COUNT
    next unless($dst_ip eq $HOST); # only inbound traffic

    $prot_hash{ $prot } = 0 unless(exists $prot_hash{ $prot });
    $prot_hash{ $prot } = $prot_hash{ $prot } + 1;

    if($prot ne 'ICMP') {
      $prot_hash{ $dst_port } = 0 unless(exists $prot_hash{ $dst_port });
      $prot_hash{ $dst_port } = $prot_hash{ $dst_port } + 1;
    }

    ($curr_time = $time) =~ s/\..*//; # remove seconds from time
    $prev_time = $curr_time if($prev_time eq "-1"); # initial case
    if($prev_time ne $curr_time) {
      while( my ($key, $value) = each %prot_hash ) {
        switch($key) {
          case 'TCP'  { print TCPFILE  "$prev_time,$key,$value\n"; }
          case 'UDP'  { print UDPFILE  "$prev_time,$key,$value\n"; } 
          case 'ICMP' { print ICMPFILE "$prev_time,$key,$value\n"; }
          case '80'   { print WEBFILE  "$prev_time,$key,$value\n"; }
          else        { print OTHFILE  "$prev_time,$key,$value\n"; }
        }
      }
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