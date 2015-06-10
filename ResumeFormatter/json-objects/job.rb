require_relative 'entry'
require_relative 'address'
require_relative 'date-range'
require_relative 'accomplishment'

class Job < Entry
  attr_accessor :company, :title, :location, :dates, :languages, :accomplishments

  def initialize(hash)
    return unless hash.respond_to? :fetch

    @company         = hash.fetch('company',         nil)
    @title           = hash.fetch('title',           nil)
    @location        = hash.fetch('location',        nil)
    @dates           = hash.fetch('dates',           nil)
    @languages       = hash.fetch('languages',       nil)
    @accomplishments = hash.fetch('accomplishments', nil)

    @location = Address.new location
    @dates    = DateRange.new @dates

    @languages       = Entry.convert @languages
    @accomplishments = Entry.convert @accomplishments, Accomplishment
  end

end
