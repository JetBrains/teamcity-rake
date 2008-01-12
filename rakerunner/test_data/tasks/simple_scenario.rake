namespace :simple_sc do

  #directory "./"

   #task :dist => :dist_dir do
   desc "Main task"
   task :dist => :dist_dir do
     puts File.new(".").path
   end
end