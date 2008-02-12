# Created by IntelliJ IDEA.
# User: Roman.Chernyatchik
# Date: 11.02.2008
# Time: 21:06:23
# To change this template use File | Settings | File Templates.
require 'rake'
require 'spec/rake/spectask'

desc "Run all examples"
Spec::Rake::SpecTask.new('spec_examples') do |t|
  t.spec_files = FileList['spec/common/**/*_spec.rb']
end

Spec::Rake::SpecTask.new('spec_examples_compilation_failure') do |t|
  t.spec_files = FileList['spec/compilation_errors/**/*_spec.rb']
end