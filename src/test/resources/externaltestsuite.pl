#! /usr/bin/perl

testOneFile("expandedparser.c", "c", "gcc");
testOneFile("packedparser.c", "c", "gcc");
testOneFile("expandedscanner.c", "c", "gcc");
testOneFile("packedscanner.c", "c", "gcc");
testOneFile("expandedparser.pas", "pascal", "fpc");
testOneFile("packedparser.pas", "pascal", "fpc");
testOneFile("expandedscanner.pas", "pascal", "fpc");
testOneFile("packedscanner.pas", "pascal", "fpc");

sub execute # (cmd)
{
  my $cmd = $_[0];
  my $s = `$cmd  2>&1`;
  my $exitCode = $?;
  
  print "executing command: $cmd\n";
  if ($exitCode > 0) 
  {
      print "executing $cmd did exit with code: $exitCode.  Results:
$s
";
    return "";
  }
  
  return $s;
}

sub testOneFile #(filename, prefix, compiler)
{
  my $filename = $_[0];
  my $prefix = $_[1];
  my $compiler = $_[2];
  
  my $outputPrefix = "-o ";
  if ($compiler eq "fpc")
  {
    $outputPrefix = "-o"; # no space
  }
  
  my $output = $filename;
  $output =~ s/\..*/.exe/;
  my $command = "$compiler $filename $outputPrefix$prefix$output";
  
  my $s = execute ($command);

  my $exe = $filename;
  $exe =~ s/\..*//;
  
  $s = execute("./$prefix$exe");
  if ($s ne "")
  {
    checkTotal($s, $filename);
  }
}

sub checkTotal # (output, filename)
{
  my $s = $_[0];
  my $filename = $_[1];
  
  my $found = 0;
  my @lines = split("\n", $s);
  foreach my $line (@lines)
  {
    if ($line =~ /.*Total.*-17.*/)
    {
      $found = 1;
    }
  }
  
  if ($found == 0)
  {
    print "Total not found on file $filename\n$s\n";
  }
  return $found;
}
