require "rake"
require "spec/rake/spectask"

########################################
namespace :output do

  Spec::Rake::SpecTask.new(:spec_output) do |t|
    t.spec_files = FileList['spec/output/**/*_spec.rb']
  end
end