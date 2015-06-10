require_relative 'entry'
require_relative 'website'

class Project < Entry
  attr_accessor :title, :language, :homepage, :mirrors, :bulletpoints

  def initialize(hash)
    return unless hash.respond_to? :fetch

    @title        = hash.fetch('title',        nil)
    @language     = hash.fetch('language',     nil)
    @homepage     = hash.fetch('homepage',     nil)
    @mirrors      = hash.fetch('mirrors',      nil)
    @bulletpoints = hash.fetch('bulletpoints', nil)

    @homepage = Website.new @homepage
    @homepage.show_delimiter = false

    @mirrors = Entry.convert @mirrors, Website
    @bulletpoints = Entry.convert @bulletpoints
  end

end
