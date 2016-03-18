#! /usr/bin/perl

testOneCountFile("expandedparser.c", "c", "gcc");
testOneCountFile("packedparser.c", "c", "gcc");
testOneCountFile("expandedscanner.c", "c", "gcc");
testOneCountFile("packedscanner.c", "c", "gcc");
testOneCountFile("expandedparser.pas", "pascal", "fpc");
testOneCountFile("packedparser.pas", "pascal", "fpc");
testOneCountFile("expandedscanner.pas", "pascal", "fpc");
testOneCountFile("packedscanner.pas", "pascal", "fpc");

testOneLexerModeFile("lexermode.c", "c", "gcc");
testOneLexerModeFile("lexermode.pas", "pascal", "fpc");

testOneRegexpTokenizerFile("regexptokenizer.c", "c", "gcc");
testOneRegexpTokenizerFile("regexptokenizer.pas", "pascal", "fpc");

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

sub testOneCountFile #(filename, prefix, compiler)
{
  my $s = runTest($_[0], $_[1], $_[2]);
  if ($s ne "")
  {
    checkTotal($s, $filename);
  }
}

sub testOneLexerModeFile #(filename, prefix, compiler)
{
  my $s = runTest($_[0], $_[1], $_[2]);
  if ($s ne "")
  {
    checkLexerMode($s, $filename);
  }
}

sub testOneRegexpTokenizerFile #(filename, prefix, compiler)
{
  my $s = runTest($_[0], $_[1], $_[2]);
  if ($s ne "")
  {
    checkRegexpTokenizer($s, $filename);
  }
}

sub runTest #(filename, prefix, compiler)
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
  return $s;
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

sub checkLexerMode # (output, filename)
{
  my $s = $_[0];
  my $filename = $_[1];
  
  my $found = 0;
  my @lines = split("\n", $s);
  foreach my $line (@lines)
  {
    if ($line =~ /.*bacaab.*/)
    {
      $found = 1;
    }
  }
  
  if ($found == 0)
  {
    print "Lexer mode result not found on file $filename\n$s\n";
  }
  return $found;
}

sub checkRegexpTokenizer # (output, filename)
{
  my $s = $_[0];
  my $filename = $_[1];
  
  my $found = 0;
  my @lines = split("\n", $s);
  foreach my $line (@lines)
  {
    if ($line =~ /.*EABCDFGIA.*/)
    {
      $found = 1;
    }
  }
  
  if ($found == 0)
  {
    print "Regexp tokenizer result not found on file $filename\n$s\n";
  }
  return $found;
}
