require_relative 'address'
require_relative 'website'

class Contact
  attr_accessor :email, :phone, :address, :websites

  def initialize(hash)
    return if !hash.respond_to? :fetch

    @email    = hash.fetch("email", nil)
    @phone    = hash.fetch("phone", nil)
    @address  = hash.fetch("address", nil)
    @websites = hash.fetch("websites", nil)

    @address  = Address.new @address
    @websites = Entry.convert @websites, Website
  end

end
