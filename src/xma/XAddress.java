/*
 * XMPP Address (http://github.com/syntelos/xma)
 * Copyright (C) 2012, John Pritchard
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package xma;

/**
 * An XMPP address is a messaging network reference
 * <pre>
 * logon = identifier@host
 * full = logon/resource
 * </pre>
 * 
 * <h3>Partial order</h3>
 * 
 * <p> The constructor completes for strings recognized as Identifier,
 * Logon (Identifier + Host), or Full (Logon + Resource).  Missing
 * components remain null after construction. </p>
 * 
 * <h3>Validation</h3>
 * 
 * <p> The only qualifications required here are in '@' and '/' as
 * required to parse strings of the form
 * <code>identifier@host/resource</code>.  Additional validation may
 * be applied by subclasses. </p>
 * 
 * <h3>Identity</h3>
 * 
 * <p> The equals method tests logon or identifier components, but not
 * the resource component.  The intent is to aid the construction of
 * rosters with a test that supports the external logic required for
 * roster management.  See <code>vector/xs</code>.
 * 
 * <p> The comparison method tests the shared subset of components
 * from full to logon or identifier.  The intent is to support list
 * sorting, where the information in the list is employed. </p>
 * 
 * <p> The hash code and char sequence (string) methods employ all
 * existing information. </p>
 * 
 * <h3>Multiple resources</h3>
 * 
 * <p> The XMPP example under <code>vector/xs</code> has multiple
 * resources for a logon, while the XS example does not. </p>
 * 
 * 
 * 
 * @see http://code.google.com/p/java-vector/wiki/XS
 * @author jdp
 */
public class XAddress
    extends java.lang.Object
    implements java.lang.CharSequence,
               java.io.Serializable,
               java.lang.Comparable<XAddress>
{
    public final static long serialVersionUID = 1L;


    /**
     * Ordered address components
     */
    public enum Require {
        /**
         * Require input having no less than identifier
         */
        Identifier, 
        /**
         * Require input having no less than identifier and host
         * (logon)
         */
        Logon, 
        /**
         * Require input having no less than identifier, host and
         * resource (logon + resource)
         */
        Full;
    }


    /**
     * Require input equal to or greater than identifier
     */
    public static class Identifier
        extends XAddress
    {
        public Identifier(String input){
            super(input,Require.Identifier);
        }
    }
    /**
     * Require input equal to or greater than logon
     */
    public static class Logon
        extends XAddress
    {
        public Logon(String input){
            super(input,Require.Logon);
        }
    }
    /**
     * Require full XMPP address input string
     */
    public static class Full
        extends XAddress
    {
        public Full(String input){
            super(input,Require.Full);
        }
    }




    public final String identifier, host, resource, logon, full;

    public final String resourceKind, resourceSession;


    /**
     * @param string A part or whole XMPP address
     */
    public XAddress(String string){
        this(string,XAddress.Require.Identifier);
    }
    /**
     * @param string A part or whole XMPP address, bounded by a
     * component list length requirement
     * @param require Input components extent requirement
     */
    public XAddress(String string, XAddress.Require require){
        super();

        final String[] parts = XAddress.Scan(string);
        if (null == parts || null == require)
            throw new IllegalArgumentException(string);
        else {

            switch(parts.length){
            case 1:
                switch (require){
                case Logon:
                case Full:
                    throw new IllegalArgumentException(string);
                default:
                    /*
                     * identifier
                     */
                    this.identifier = parts[0];
                    this.host = null;
                    this.resource = null;
                    this.resourceKind = null;
                    this.resourceSession = null;
                    break;
                }
                break;
            case 2:
                switch (require){
                case Full:
                    throw new IllegalArgumentException(string);
                default:
                    /*
                     * identifier, host
                     */
                    this.identifier = parts[0];
                    this.host = parts[1];
                    this.resource = null;
                    this.resourceKind = null;
                    this.resourceSession = null;
                    break;
                }
                break;
            case 3:
                /*
                 * identifier, host, resource
                 */
                this.identifier = parts[0];
                this.host = parts[1];
                this.resource = parts[2];
                String[] components = Resource(this.resource);
                if (null != components){
                    this.resourceKind = components[0];
                    this.resourceSession = components[1];
                }
                else {
                    this.resourceKind = this.resource;
                    this.resourceSession = this.resource;
                }
                break;
            default:
                throw new IllegalStateException();
            }
            /*
             */
            final StringBuilder strbuf = new StringBuilder();
            strbuf.append(this.identifier);

            if (null != this.host){
                strbuf.append('@');
                strbuf.append(this.host);

                this.logon = strbuf.toString();

                if (null != this.resource){

                    strbuf.append('/');
                    strbuf.append(this.resource);

                    this.full = strbuf.toString();
                }
                else
                    this.full = null;
            }
            else {
                this.logon = null;
                this.full = null;
            }
        }
    }


    public int length(){
        if (null != this.full)
            return this.full.length();
        else if (null != this.logon)
            return this.logon.length();
        else
            return this.identifier.length();
    }
    public char charAt(int index){
        if (null != this.full)
            return this.full.charAt(index);
        else if (null != this.logon)
            return this.logon.charAt(index);
        else
            return this.identifier.charAt(index);
    }
    public CharSequence subSequence(int start, int end){
        if (null != this.full)
            return this.full.subSequence(start,end);
        else if (null != this.logon)
            return this.logon.subSequence(start,end);
        else
            return this.identifier.subSequence(start,end);
    }
    public int hashCode(){
        if (null != this.full)
            return this.full.hashCode();
        else if (null != this.logon)
            return this.logon.hashCode();
        else
            return this.identifier.hashCode();
    }
    public String toString(){
        if (null != this.full)
            return this.full;
        else if (null != this.logon)
            return this.logon;
        else
            return this.identifier;
    }
    public boolean equals(Object that){
        if (this == that)
            return true;
        else if (that instanceof XAddress)

            return this.equals( (XAddress)that);

        else if (null == this.host)
            return this.identifier.equals(that);
        else
            return this.logon.equals(that);
    }
    public boolean equals(XAddress that){
        if (this == that)
            return true;
        else if (null == that)
            return false;
        else if (null == this.resource || null == that.resource){

            if (null == this.host || null == that.host)
                return this.identifier.equals(that.identifier);
            else
                return this.logon.equals(that.logon);
        }
        else if (this.logon.equals(that.logon)){

            if (null != this.resource && null != that.resource)
                return this.resourceKind.equals(that.resourceKind);
            else
                return true;
        }
        else
            return false;
    }
    public int compareTo(XAddress that){
        if (this == that)
            return 0;
        else if (null == this.resource || null == that.resource){

            if (null == this.host || null == that.host)
                return this.identifier.compareTo(that.identifier);
            else
                return this.logon.compareTo(that.logon);
        }
        else {
            return this.full.compareTo(that.full);
        }
    }


    /**
     * Parse XAddress
     * 
     * @param string XMPP address string
     * @return null, or one, two, or three elements
     */
    public final static String[] Scan(String string){
        if (null == string)
            return null;
        else {
            final char[] cary = string.toCharArray();
            final int carlen = cary.length;
            if (0 < carlen){
                /*
                 * measure
                 */
                int indexOfSlash = -1, indexOfHost = -1;
                scan:
                for (int cc = 0; cc < carlen; cc++){

                    switch(cary[cc]){

                    case '/':
                        if (0 < indexOfHost && (indexOfHost+1) < cc){

                            indexOfSlash = cc;

                            break scan;
                        }
                        else
                            throw new IllegalArgumentException(string);

                    case '@':
                        if (-1 == indexOfHost)
                            indexOfHost = cc;
                        else
                            throw new IllegalArgumentException(string);
                        break;
                    default:
                        break;
                    }
                }
                /*
                 * cut
                 */
                if (-1 < indexOfHost){
                    if (-1 < indexOfSlash){
                        return new String[]{
                            string.substring(0,indexOfHost),
                            string.substring(indexOfHost+1,indexOfSlash),
                            string.substring(indexOfSlash+1)
                        };
                    }
                    else {
                        return new String[]{
                            string.substring(0,indexOfHost),
                            string.substring(indexOfHost+1)
                        };
                    }
                }
                else {
                    return new String[]{
                        string
                    };
                }
            }
            else
                return null;
        }
    }
    public final static String[] Resource(String string){
        if (null != string){
            final int dot = string.lastIndexOf('.');
            if (0 < dot){
                return new String[]{
                    string.substring(0,dot),
                    string.substring(dot+1)
                };
            }
            else {
                final char[] cary = string.toCharArray();
                final int carlen = cary.length;
                if (0 < carlen){

                    for (int cc = (carlen-1); -1 < cc; cc--){

                        char ch = cary[cc];

                        if (IsNotHex(ch)){

                            final int end;
                            /*
                             * String terminal in "android" is in HEX
                             */
                            if (5 == cc && 'i' == ch && 'a' == cary[0] && 'n' == cary[1])
                                end = (cc + 2);
                            /*
                             * String terminal in "iPhone" is in HEX
                             */
                            else if (4 == cc && 'n' == ch && 'i' == cary[0] && 'P' == cary[1])
                                end = (cc + 2);
                            /*
                             * Others not having a HEX terminal
                             */
                            else
                                end = (cc + 1);

                            return new String[]{
                                string.substring(0,end),
                                string.substring(end)
                            };
                        }
                    }
                }
            }
        }
        return null;
    }
    public final static boolean IsNotHex(char ch){
        if ('a' <= ch && 'f' >= ch)
            return false;
        else if ('A' <= ch && 'F' >= ch)
            return false;
        else if ('0' <= ch && '9' >= ch)
            return false;
        else
            return true;
    }
}
